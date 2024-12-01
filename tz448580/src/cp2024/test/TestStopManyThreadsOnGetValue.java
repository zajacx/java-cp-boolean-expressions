package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class TestStopManyThreadsOnGetValue {

    private static class Stopper implements Runnable {
        
        private final CircuitSolver solver;

        public Stopper(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Stopper interrupted");
                return;
            }
            System.out.println("[Stopper] Stopping solver...");
            solver.stop();
            System.out.println("[Stopper] Solver stopped.");
        }
    }

    private static class Creator implements Runnable {
        
        private final CircuitSolver solver;

        public Creator(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(3)));
            System.out.println("[Creator] Called solve()...");
            CircuitValue cv = solver.solve(c);
            Boolean val;
            List<Thread> waiters = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Thread waiter = new Thread(new Waiter(cv));
                waiters.add(waiter);
                System.out.println("[Creator] Starting waiter " + waiter.getName());
                waiter.start();
            }
            try {
                val = cv.getValue();
                for (Thread waiter : waiters) {
                    waiter.join();
                }

            } catch (InterruptedException e) {
                System.out.println("[Creator] Interrupted");
                return;
            }
            System.out.println("[Creator] Solved 1: " + val);
        }

        private static class Waiter implements Runnable {
        
            private final CircuitValue value;
    
            public Waiter(CircuitValue value) {
                this.value = value;
            }
    
            @Override
            public void run() {
                System.out.println("[Waiter " + Thread.currentThread().getName() + "] Called getValue()...");
                Boolean val;
                try {
                    val = value.getValue();
                } catch (InterruptedException e) {
                    System.out.println("[Waiter " + Thread.currentThread().getName() + "] Interrupted");
                    return;
                }
                System.out.println("[Waiter] Solved 1: " + val);
            }
        }
    }

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();
        Thread stopper = new Thread(new Stopper(solver));
        Thread creator = new Thread(new Creator(solver));

        stopper.start();
        creator.start();

        try {
            stopper.join();
            creator.join();
        } catch (InterruptedException e) {
            System.out.println("Main interrupted");
        }

    }
}

