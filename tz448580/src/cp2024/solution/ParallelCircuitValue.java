/**
 * ParallelCircuitValue implementation.
 * Author: Tomasz Zajac <tz448580@students.mimuw.edu.pl>
 */

package cp2024.solution;

import cp2024.circuit.CircuitValue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/** 
 * Implementation of the CircuitValue interface.
 */
public class ParallelCircuitValue implements CircuitValue {

    // Circuit value, semaphore to protect it.
    private Boolean value;
    private final Semaphore forValue;
    
    // Queue of threads waiting for value, semaphore to protect it.
    private final Queue<Thread> waitingForValue;
    private final Semaphore queueMutex;

    // Flag that indicates if the solver was stopped during computations, semaphore to protect it.
    private Boolean isBroken;
    private final Semaphore flagMutex;

    // Thread that runs the circuit.
    private Thread circuitThread;
    
    public ParallelCircuitValue(Semaphore forValue) {
        this.value = null;
        this.forValue = forValue;
        this.waitingForValue = new LinkedList<>();
        this.queueMutex = new Semaphore(1, true); 
        this.circuitThread = null;
        this.isBroken = false;
        this.flagMutex = new Semaphore(1, true);
    }

    @Override
    public boolean getValue() throws InterruptedException {
        // Check if there was an interruption during computations:
        // Important: this flag isn't set to true by stop() if computation is finished.
        Boolean flagState;
        try {
            flagMutex.acquire();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }
        // Here we can read the flag state:
        flagState = isBroken;
        flagMutex.release();

        // Solver was stopped and didn't compute this value:
        if (flagState) {
            throw new InterruptedException();
        }

        // Here we are sure that the value was computed/is being computed:
        try {
            queueMutex.acquire();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }
        // Add the thread to the queue of waiting threads:
        waitingForValue.add(Thread.currentThread());
        queueMutex.release();

        // Wait for the value to be computed:
        Boolean result;
        try {
            // We can wait here for a long time:
            forValue.acquire();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }
        result = value;
        // From now on, the value of the circuit is visible immediately.
        forValue.release();
        return result;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public void setCircuitThread(Thread circuitThread) {
        this.circuitThread = circuitThread;
    }

    public void setIsBroken() {
        this.isBroken = true;
    }

    public Thread getCircuitThread() {
        return circuitThread;
    }

    public Queue<Thread> getQueue() {
        return waitingForValue;
    }

    public Semaphore getQueueMutex() {
        return queueMutex;
    }

    public Semaphore getFlagMutex() {
        return flagMutex;
    }

    public Boolean getIsBroken() {
        return isBroken;
    }

}

