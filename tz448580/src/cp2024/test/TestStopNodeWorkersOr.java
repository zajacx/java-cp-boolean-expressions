package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestStopNodeWorkersOr {

    private static class Worker implements Runnable {
        
        private final CircuitSolver solver;

        public Worker(CircuitSolver solver) {
            this.solver = solver;
        }

        @Override
        public void run() {
            Circuit c = new Circuit(CircuitNode.mk(NodeType.OR,
                                        CircuitNode.mk(NodeType.OR,
                                            CircuitNode.mk(false, Duration.ofSeconds(4)),
                                            CircuitNode.mk(false, Duration.ofSeconds(4)),
                                            CircuitNode.mk(false, Duration.ofSeconds(4)),
                                            CircuitNode.mk(false, Duration.ofSeconds(5))),
                                        CircuitNode.mk(NodeType.OR,
                                            CircuitNode.mk(false, Duration.ofSeconds(4)),
                                            CircuitNode.mk(false, Duration.ofSeconds(4)),
                                            CircuitNode.mk(false, Duration.ofSeconds(4)),
                                            CircuitNode.mk(true, Duration.ofSeconds(6)))));
            System.out.println("[Worker] Solving...");
            CircuitValue value = solver.solve(c);
            Boolean val;
            try {
                val = value.getValue();
            } catch (InterruptedException e) {
                System.out.println("[Worker] Interrupted");
                return;
            }
            System.out.println("[Worker] Solved: " + val);
        }
    }

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();

        Thread worker = new Thread(new Worker(solver));
        worker.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Main interrupted");
            return;
        }
        System.out.println("[Main] Stopping worker...");
        solver.stop();
        System.out.println("[Main] Worker stopped.");
    }
    
}

