package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestStopManyWorkers {

    private static class Stopper implements Runnable {
        
        private final CircuitSolver solver;

        public Stopper(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            System.out.println("[Stopper] Stopping solver after 2 seconds...");
            solver.stop();
            System.out.println("[Stopper] Solver stopped.");
        }
    }

    private static class FirstWorker implements Runnable {
        
        private final CircuitSolver solver;

        public FirstWorker(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(1)));
            System.out.println("[Worker 1] Solving 1...");
            CircuitValue first = solver.solve(c);
            Boolean firstValue;
            try {
                firstValue = first.getValue();
            } catch (InterruptedException e) {
                System.out.println("[Worker 1] Interrupted");
                return;
            }
            System.out.println("[Worker 1] Solved 1: " + firstValue);
        }
    }

    private static class SecondWorker implements Runnable {
        
        private final CircuitSolver solver;

        public SecondWorker(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(4)));
            System.out.println("[Worker 2] Solving 2...");
            CircuitValue second = solver.solve(c);
            Boolean secondValue;
            try {
                secondValue = second.getValue();
            } catch (InterruptedException e) {
                System.out.println("[Worker 2] Interrupted");
                return;
            }
            System.out.println("[Worker 2] Solved 2: " + secondValue);
        }
    }

    private static class ThirdWorker implements Runnable {
        
        private final CircuitSolver solver;

        public ThirdWorker(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(5)));
            System.out.println("[Worker 3] Solving 3...");
            CircuitValue third = solver.solve(c);
            Boolean thirdValue;
            try {
                thirdValue = third.getValue();
            } catch (InterruptedException e) {
                System.out.println("[Worker 3] Interrupted");
                return;
            }
            System.out.println("[Worker 3] Solved 3: " + thirdValue);
        }
    }

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();
        Thread stopper = new Thread(new Stopper(solver));
        Thread firstWorker = new Thread(new FirstWorker(solver));
        Thread secondWorker = new Thread(new SecondWorker(solver));
        Thread thirdWorker = new Thread(new ThirdWorker(solver));

        firstWorker.start();
        secondWorker.start();
        thirdWorker.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Main interrupted");
        }

        stopper.start();

        try {
            firstWorker.join();
            secondWorker.join();
            thirdWorker.join();
            stopper.join();
        } catch (InterruptedException e) {
            System.out.println("Main interrupted");
        }

    }
}

