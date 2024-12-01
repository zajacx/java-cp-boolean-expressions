package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestSolveConcurrent {

    private static class First implements Runnable {

        private final CircuitSolver solver;

        public First(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(NodeType.OR, 
                                        CircuitNode.mk(false, Duration.ofSeconds(3)),
                                        CircuitNode.mk(false, Duration.ofSeconds(5))));
            System.out.println("Solving 1...");
            CircuitValue first = solver.solve(c);
            Boolean firstValue;
            try {
                firstValue = first.getValue();
            } catch (InterruptedException e) {
                System.out.println("[1] Interrupted");
                return;
            }
            System.out.println("Solved 1: " + firstValue);
        }
    }

    private static class Second implements Runnable {

        private final CircuitSolver solver;

        public Second(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(NodeType.AND, 
                                        CircuitNode.mk(false, Duration.ofSeconds(2)),
                                        CircuitNode.mk(false, Duration.ofSeconds(7))));
            System.out.println("Solving 2...");
            CircuitValue second = solver.solve(c);
            Boolean secondValue;
            try {
                secondValue = second.getValue();
            } catch (InterruptedException e) {
                System.out.println("[2] Interrupted");
                return;
            }
            System.out.println("Solved 2: " + secondValue);
        }
    }

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();

        Thread t1 = new Thread(new First(solver));
        Thread t2 = new Thread(new Second(solver));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("[m] Interrupted");
        }

        solver.stop();

    }

}