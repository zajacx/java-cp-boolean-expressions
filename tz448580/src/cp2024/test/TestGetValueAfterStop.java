package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestGetValueAfterStop {

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

    private static class Worker implements Runnable {
        
        private final CircuitSolver solver;

        public Worker(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(3)));
            System.out.println("[Worker] Solving 1...");
            CircuitValue first = solver.solve(c);
            Boolean firstValue;
            try {
                firstValue = first.getValue();
            } catch (InterruptedException e) {
                System.out.println("[Worker] Interrupted");
                return;
            }
            System.out.println("[Worker] Solved 1: " + firstValue);
        }
    }

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();
        Thread stopper = new Thread(new Stopper(solver));
        Thread worker = new Thread(new Worker(solver));

        stopper.start();
        worker.start();

        try {
            stopper.join();
            worker.join();
        } catch (InterruptedException e) {
            System.out.println("Main interrupted");
        }

    }
}
