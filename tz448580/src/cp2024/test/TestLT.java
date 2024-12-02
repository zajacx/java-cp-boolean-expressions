package cp2024.test;

import cp2024.solution.ParallelCircuitSolver;
import cp2024.circuit.*;
import cp2024.utils.Tester;

public class TestLT {

    public static void main(String[] args) {
        ParallelCircuitSolver solver = new ParallelCircuitSolver();
        Tester tester = new Tester();
        
        // Test 1: Immediate positive.
        Circuit c = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false)));
        System.out.println("Solving 1...");
        CircuitValue first = solver.solve(c);
        Boolean firstValue;
        try {
            firstValue = first.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 1: " + firstValue);
        tester.check(firstValue, true);

        // Test 2: Immediate negative.
        c = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false)));
        System.out.println("Solving 2...");
        CircuitValue second = solver.solve(c);
        Boolean secondValue;
        try {
            secondValue = second.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 2: " + secondValue);
        tester.check(secondValue, false);
    }
}
