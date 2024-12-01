package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TestManyThreadsGetValueCorrectly {

    private static class Creator implements Runnable {
        
        private final CircuitSolver solver;

        public Creator(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(12)));
            System.out.println("[Creator] Called solve()...");
            CircuitValue cv = solver.solve(c);
            Boolean val;
            List<Thread> waiters = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Thread waiter = new Thread(new Waiter(cv, i));
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
            private final int delay;
    
            public Waiter(CircuitValue value, int delay) {
                this.value = value;
                this.delay = delay;
            }
    
            @Override
            public void run() {
                System.out.println("[Waiter " + Thread.currentThread().getName() + "] Called getValue()...");
                Boolean val;
                try {
                    Thread.sleep(1000 * delay);
                    val = value.getValue();
                    Thread.sleep(1000 * delay);
                } catch (InterruptedException e) {
                    System.out.println("[Waiter " + Thread.currentThread().getName() + "] Interrupted");
                    return;
                }
                System.out.println("[Waiter " + Thread.currentThread().getName() + " ] Solved 1: " + val);
            }
        }
    }

    public static void main(String[] args) {
        CircuitSolver solver = new ParallelCircuitSolver();
        Thread creator = new Thread(new Creator(solver));
        creator.start();
        try {
            creator.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
    
}
