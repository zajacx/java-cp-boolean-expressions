/**
 * ParallelCircuitSolver implementation.
 * Author: Tomasz Zajac <tz448580@students.mimuw.edu.pl>
 */

package cp2024.solution;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.LeafNode;
import cp2024.circuit.NodeType;
import cp2024.circuit.ThresholdNode;

import cp2024.demo.BrokenCircuitValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;


public class ParallelCircuitSolver implements CircuitSolver {

    // To stop all threads working on circuits, we store them here:
    private final List<ParallelCircuitValue> circuitValues = new ArrayList<>();

    // Mutex to protect circuitValues:
    private final Semaphore circuitValuesMutex = new Semaphore(1, true);

    // Solver state:
    private Boolean state = true;
    
    // State mutex:
    private final Semaphore stateMutex = new Semaphore(1, true);

    // ------------------------------------ Public methods ------------------------------------

    // Immediately returns a CircuitValue object that represents
    // the result of solving the circuit.
    @Override
    public CircuitValue solve(Circuit c) {

        try {
            stateMutex.acquire();                           // Acquire state mutex.
        } catch (InterruptedException e) {
            return new BrokenCircuitValue();
        }

        Boolean acceptComputations = state;                 // Save the state.
        stateMutex.release();                               // Release the mutex.
        
        if (!acceptComputations) {
            return new BrokenCircuitValue();                // Return a dummy object for each new request. 
        } else {
            Semaphore forValue = new Semaphore(0, true);    // Semaphore to protect the value field.
            ParallelCircuitValue circuitValue = new ParallelCircuitValue(forValue);
            Runnable circuitWorker = new CircuitWorker(c, forValue, circuitValue);
            Thread circuitThread = new Thread(circuitWorker);

            circuitValue.setCircuitThread(circuitThread);   // Store the thread.

            try {
                circuitValuesMutex.acquire();               // Acquire circuitValues mutex.
            } catch (InterruptedException e) {
                return new BrokenCircuitValue();
            }
            circuitValues.add(circuitValue);                // Store the CircuitValue object.
            circuitValuesMutex.release();                   // Release the mutex.
            circuitThread.start();                          // Start the computation of the circuit c.
            return circuitValue;
        }
    }

    @Override
    public void stop() {
        // Iterate through all CircuitValue objects and interrupt threads
        // that manage all circuits. Also interrupt a thread that is waiting
        // for a value of a circuit (has called getValue() method).

        // Acquire state mutex:
        try {
            stateMutex.acquire();
        } catch (InterruptedException e) {
            return;
        }

        if (!state) {
            // Solver has already stopped.
            stateMutex.release();
        } else {
            // Solver is working - stop it.
            state = false;
            stateMutex.release();
            try {
                circuitValuesMutex.acquire();
            } catch (InterruptedException e) {
                // This thread won't be interrupted.
            }
            // Interrupt all alive circuit threads.
            for (ParallelCircuitValue circuitValue : circuitValues) {
                Thread circuitThread = circuitValue.getCircuitThread();
                Semaphore flagMutex = circuitValue.getFlagMutex();
                if (circuitThread.isAlive()) {
                    try {
                        flagMutex.acquire();
                    } catch (InterruptedException e) {
                        // This thread won't be interrupted.
                    }
                    circuitValue.setIsBroken();
                    flagMutex.release();
                    // Finally - interrupt alive circuitThread:
                    circuitThread.interrupt();
                    // circuitThread will be interrupted and will see brokenFlag set to true,
                    // which will cause interrupting all waiting external threads.
                }
            }
            circuitValuesMutex.release();
        }
    }

    // ----------------------------------- Runnable classes -----------------------------------

    /**
     * CircuitWorker:
     * Thread executing this code initializes the computation of the circuit.
     * After the computation is done, it sets the value of the circuit in an
     * object that implements the CircuitValue interface.
     * It also works as a broker between the circuit and the external thread.
     * If the external thread calls getValue() method, it waits to be woken up
     * by the CircuitWorker thread.
     * If the external thread is interrupted, it interrupts CircuitWorker that
     * is responsible for interrupting all threads that work on the circuit.
     */
    private static class CircuitWorker implements Runnable {

        private final Circuit circuit;
        private final Semaphore forValue;   // Some external threads wait here.
        private final ParallelCircuitValue circuitValue;
        private final BlockingQueue<Boolean> myQueue;
        
        public CircuitWorker(
            Circuit circuit,
            Semaphore forValue,
            ParallelCircuitValue circuitValue
        ) {
            this.circuit = circuit;
            this.forValue = forValue;
            this.circuitValue = circuitValue;
            this.myQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            // Create a thread for the root node of the circuit.
            CircuitNode root = circuit.getRoot();
            Runnable rootWorker;
            try {
                rootWorker = determineWorkerType(root, myQueue);
            } catch (RuntimeException e) {
                // There won't be nodes with illegal types.
                return;
            }
            
            Thread rootThread = new Thread(rootWorker);
            Boolean value;
            rootThread.start();

            Queue<Thread> waitingForValue = circuitValue.getQueue();
            Semaphore queueMutex = circuitValue.getQueueMutex();
            Semaphore flagMutex = circuitValue.getFlagMutex();

            // Wait for the root node to finish its work.
            try {
                value = myQueue.take();
            } catch (InterruptedException e) {
                // Cancel all computations if stop() was called <=> brokenFlag is set.
                Boolean brokenFlagValue;
                try {
                    flagMutex.acquire();
                } catch (InterruptedException ex) {
                    // This thread won't be interrupted.
                }
                brokenFlagValue = circuitValue.getIsBroken();
                flagMutex.release();

                // Check the flag.
                if (brokenFlagValue) {
                    // Interrupt the root so it can stop the computations.
                    rootThread.interrupt();
                    // Interrupt all threads that are waiting on forValue semaphore.
                    // Acquire queueMutex, interrupt all threads and remove them from the queue.
                    try {
                        queueMutex.acquire();
                    } catch (InterruptedException ex) {
                        // Do nothing.
                    }
                    while (waitingForValue.size() > 0) {
                        Thread waiting = waitingForValue.poll();
                        waiting.interrupt();
                    }
                    queueMutex.release();
                    Thread.currentThread().interrupt();
                }
                return;
            }
            
            // If successful, set the value of the circuit.
            circuitValue.setValue(value);
            forValue.release();
        }
    }

    /**
     * LeafWorker:
     * Thread executing this code works on a node that represents a leaf.
     */
    private static class LeafWorker implements Runnable {
            
        private final LeafNode node;
        private final BlockingQueue<Boolean> parentQueue;
    
        public LeafWorker(
            LeafNode node,
            BlockingQueue<Boolean> parentQueue
        ) {
            this.node = node;
            this.parentQueue = parentQueue;
        }

        @Override
        public void run() {
            try {
                Boolean value = node.getValue();
                parentQueue.put(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * SimpleWorker:
     * Thread executing this code works on a node that represents one of these:
     * - NOT
     * - AND
     * - OR
     * It uses one blocking queue to communicate with child nodes.
     */
    private static class SimpleWorker implements Runnable {
            
        private final CircuitNode node;
        private final BlockingQueue<Boolean> parentQueue;
        private final BlockingQueue<Boolean> myQueue;
        private final List<Thread> childThreads;
    
        public SimpleWorker(
            CircuitNode node,
            BlockingQueue<Boolean> parentQueue
        ) {
            this.node = node;
            this.parentQueue = parentQueue;
            this.myQueue = new LinkedBlockingQueue<>();
            this.childThreads = new ArrayList<>();
        }

        @Override
        public void run() {
            // Get node type.
            NodeType type = node.getType();

            // Get children of the current node.
            CircuitNode[] childNodes;
            try {
                childNodes = node.getArgs();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            int valuesLeft = childNodes.length;

            // Create a thread for each child node.
            for (CircuitNode child : childNodes) {
                Runnable childWorker;
                try {
                    childWorker = determineWorkerType(child, myQueue);
                } catch (RuntimeException e) {
                    // Illegal node type.
                    continue;
                }
                Thread childThread = new Thread(childWorker);
                childThreads.add(childThread);
                childThread.start();
            }

            Boolean value;
            
            // Case: NOT
            if (type == NodeType.NOT) {
                try {
                    value = !myQueue.take();
                } catch (InterruptedException e) {
                    for (Thread child : childThreads) {
                        child.interrupt();
                    }
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            // Case: AND
            else if (type == NodeType.AND) {
                value = true;
                while (valuesLeft > 0) {
                    Boolean childValue;
                    try {
                        childValue = myQueue.take();
                    } catch (InterruptedException e) {
                        for (Thread child : childThreads) {
                            child.interrupt();
                        }
                        Thread.currentThread().interrupt();
                        return;
                    }
                    value = value && childValue;
                    if (!value) {
                        for (Thread child : childThreads) {
                            child.interrupt();
                        }
                        break;
                    }
                    valuesLeft--;
                }
            }
            // Case: OR
            else {
                value = false;
                while (valuesLeft > 0) {
                    Boolean childValue;
                    try {
                        childValue = myQueue.take();
                    } catch (InterruptedException e) {
                        for (Thread child : childThreads) {
                            child.interrupt();
                        }
                        Thread.currentThread().interrupt();
                        return;
                    }
                    value = value || childValue;
                    if (value) {
                        for (Thread child : childThreads) {
                            child.interrupt();
                        }
                        break;
                    }
                    valuesLeft--;
                }
            }

            // Put the value in the parent queue.
            try {
                parentQueue.put(value);
            } catch (InterruptedException e) {
                for (Thread child : childThreads) {
                    child.interrupt();
                }
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * ConditionHelper:
     * Thread executing this code is in the middle of ConditionWorker-child
     * communication. It is used to pass a value from one of its three children
     * to the parent ConditionWorker.
     */
    private static class ConditionHelper implements Runnable {

        private final BlockingQueue<ConditionPair> parentQueue;
        private final BlockingQueue<Boolean> helperQueue;
        private final Integer childNumber;

        public ConditionHelper(
            BlockingQueue<ConditionPair> parentQueue,
            Integer childNumber
        ) {
            this.parentQueue = parentQueue;
            this.helperQueue = new LinkedBlockingQueue<>();
            this.childNumber = childNumber;
        }

        @Override
        public void run() {
            Boolean value;
            try {
                value = helperQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                parentQueue.put(new ConditionPair(childNumber, value));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        public BlockingQueue<Boolean> getHelperQueue() {
            return helperQueue;
        }
    }

    /**
     * ConditionWorker:
     * Thread executing this code works on a node that represents IF.
     * It uses three helper threads to communicate with child nodes.
     */
    private static class ConditionWorker implements Runnable {
            
        private final CircuitNode node;
        private final BlockingQueue<Boolean> parentQueue;
        private final BlockingQueue<ConditionPair> myQueue;
        private final List<Thread> childThreads;
        private final List<Thread> helperThreads;
    
        public ConditionWorker (
            CircuitNode node,
            BlockingQueue<Boolean> parentQueue
        ) {
            this.node = node;
            this.parentQueue = parentQueue;
            this.myQueue = new LinkedBlockingQueue<>();
            this.childThreads = new ArrayList<>();
            this.helperThreads = new ArrayList<>();
        }

        @Override
        public void run() {
            // Get node type.
            // NodeType type = node.getType();

            // Get children of the current node.
            CircuitNode[] childNodes;
            try {
                childNodes = node.getArgs();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            // Create a helper thread for each child node.
            List<ConditionHelper> helpers = new ArrayList<>();

            ConditionHelper conditionHelper = new ConditionHelper(myQueue, 0);
            BlockingQueue<Boolean> conditionQueue = conditionHelper.getHelperQueue();
            helpers.add(conditionHelper);

            ConditionHelper positiveHelper = new ConditionHelper(myQueue, 1);
            BlockingQueue<Boolean> positiveQueue = positiveHelper.getHelperQueue();
            helpers.add(positiveHelper);

            ConditionHelper negativeHelper = new ConditionHelper(myQueue, 2);
            BlockingQueue<Boolean> negativeQueue = negativeHelper.getHelperQueue();
            helpers.add(negativeHelper);

            // Create a thread for each child node.
            List<Runnable> workers = new ArrayList<>();
            workers.add(determineWorkerType(childNodes[0], conditionQueue));
            workers.add(determineWorkerType(childNodes[1], positiveQueue));
            workers.add(determineWorkerType(childNodes[2], negativeQueue));

            // Start helpers and save their threads in helperThreads.
            for (ConditionHelper helper : helpers) {
                Thread helperThread = new Thread(helper);
                helperThreads.add(helperThread);
                helperThread.start();
            }

            // Start workers and save their threads in childThreads.
            for (Runnable worker : workers) {
                Thread childThread = new Thread(worker);
                childThreads.add(childThread);
                childThread.start();
            }

            Integer[] receivedValues = {-1, -1, -1};

            for (int i = 0; i < 3; i++) {

                ConditionPair pair;
                
                try {
                    pair = myQueue.take();

                } catch (InterruptedException e) {
                    for (Thread helper : helperThreads) {
                        helper.interrupt();
                    }
                    for (Thread child : childThreads) {
                        child.interrupt();
                    }
                    Thread.currentThread().interrupt();
                    return;
                }

                Integer childNumber = pair.getChildNumber();
                Boolean value = pair.getValue();
                receivedValues[childNumber] = value ? 1 : 0;

                // Condition is evaluated and we choose 'true' path:
                if (receivedValues[0] == 1 && receivedValues[1] != -1) {
                    for (Thread helper : helperThreads) {
                        helper.interrupt();
                    }
                    for (Thread child : childThreads) {
                        child.interrupt();
                    }
                    // Put into parent's queue:
                    try {
                        parentQueue.put(receivedValues[1] == 1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }

                // Condition is evaluated and we choose 'false' path:
                if (receivedValues[0] == 0 && receivedValues[2] != -1) {
                    for (Thread helper : helperThreads) {
                        helper.interrupt();
                    }
                    for (Thread child : childThreads) {
                        child.interrupt();
                    }
                    // Put into parent's queue:
                    try {
                        parentQueue.put(receivedValues[2] == 1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }


                // Lazy evaluation:
                if (receivedValues[1] != -1 && receivedValues[2] != -1 && receivedValues[1] == receivedValues[2]) {
                    for (Thread helper : helperThreads) {
                        helper.interrupt();
                    }
                    for (Thread child : childThreads) {
                        child.interrupt();
                    }
                    // Put into parent's queue:
                    try {
                        parentQueue.put(receivedValues[1] == 1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }
            }
        }
    }

    /**
     * ThresholdWorker:
     * Thread executing this code works on a node that represents one of these:
     * - GTx
     * - LTx
     * It uses one blocking queue to communicate with child nodes.
     */
    private static class ThresholdWorker implements Runnable {
            
        private final ThresholdNode node;
        private final BlockingQueue<Boolean> parentQueue;
        private final BlockingQueue<Boolean> myQueue;
        private final List<Thread> childThreads;
    
        public ThresholdWorker(
            ThresholdNode node,
            BlockingQueue<Boolean> parentQueue
        ) {
            this.node = node;
            this.parentQueue = parentQueue;
            this.myQueue = new LinkedBlockingQueue<>();
            this.childThreads = new ArrayList<>();
        }

        @Override
        public void run() {
            // Get node type.
            NodeType type = node.getType();

            // Get threshold.
            int threshold = node.getThreshold();

            // Get children of the current node.
            CircuitNode[] childNodes;
            try {
                childNodes = node.getArgs();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            int numChildren = childNodes.length;
            int valuesLeft = childNodes.length;
            Boolean value;

            // Check if there is a need to evaluate the circuit.
            if (type == NodeType.GT && numChildren <= threshold) {
                value = false;
            } 
            else if (type == NodeType.LT && numChildren < threshold) {
                value = true;
            }
            else {
                // Create a thread for each child node.
                for (CircuitNode child : childNodes) {
                    Runnable childWorker;
                    try {
                        childWorker = determineWorkerType(child, myQueue);
                    } catch (RuntimeException e) {
                        // Illegal node type.
                        continue;
                    }
                    Thread childThread = new Thread(childWorker);
                    childThreads.add(childThread);
                    childThread.start();
                }

                int gotTrue = 0;
                int gotFalse = 0;

                while (valuesLeft > 0) {
                    Boolean childValue;
                    try {
                        childValue = myQueue.take();
                    } catch (InterruptedException e) {
                        for (Thread child : childThreads) {
                            child.interrupt();
                        }
                        Thread.currentThread().interrupt();
                        return;
                    }
                    // Count new value:
                    if (childValue) {
                        gotTrue++;
                    } else {
                        gotFalse++;
                    }
                    // Check if we can break the loop:
                    if (type == NodeType.GT) {
                        if (gotTrue > threshold) {
                            value = true;
                            break;
                        } else if (gotFalse >= numChildren - threshold) {
                            value = false;
                            break;
                        }
                    } else {
                        if (gotTrue >= threshold) {
                            value = false;
                            break;
                        } else if (gotFalse > numChildren - threshold) {
                            value = true;
                            break;
                        }
                    }
                    valuesLeft--;
                }
                // Determine the value:
                if (type == NodeType.GT) {
                    value = gotTrue > threshold;
                } else {
                    value = gotTrue < threshold;
                }
            }

            // Stop all child threads.
            for (Thread child : childThreads) {
                child.interrupt();
            }

            // Put the value in the parent queue.
            try {
                parentQueue.put(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ------------------------------------ Private methods ------------------------------------

    private static class ConditionPair {
        private final Integer childNumber;
        private final Boolean value;

        public ConditionPair(Integer childNumber, Boolean value) {
            this.childNumber = childNumber;
            this.value = value;
        }

        public Integer getChildNumber() {
            return childNumber;
        }

        public Boolean getValue() {
            return value;
        }
    }

    private static Runnable determineWorkerType(CircuitNode node, BlockingQueue<Boolean> myQueue) throws RuntimeException {
        NodeType nodeType = node.getType();
        switch (nodeType) {
            case LEAF:
                return new LeafWorker((LeafNode) node, myQueue);
            case AND, OR, NOT:
                return new SimpleWorker(node, myQueue);
            case IF:
                return new ConditionWorker(node, myQueue);
            case GT, LT:
                return new ThresholdWorker((ThresholdNode) node, myQueue);
            default:
                throw new RuntimeException("Illegal type " + nodeType);
        }
    }

}

