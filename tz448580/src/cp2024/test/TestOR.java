package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestOR {

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();

        // Test 1: Immediate false and false.
        Circuit c = new Circuit(CircuitNode.mk(NodeType.OR, CircuitNode.mk(false), CircuitNode.mk(false)));
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
        assert (firstValue == true);
        Boolean firstValueRetake;
        try {
            firstValueRetake = first.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Retake: " + firstValueRetake);
        assert (firstValue == true);

        // Test 2: Immediate false and delayed true.
        c = new Circuit(CircuitNode.mk(NodeType.OR, CircuitNode.mk(true, Duration.ofSeconds(3)), CircuitNode.mk(false)));
        System.out.println("Solving 2...");
        CircuitValue second = solver.solve(c);
        Boolean secondValue;
        try {
            secondValue = second.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 2.: " + secondValue);
        assert (secondValue == false);
        Boolean secondValueRetake;
        try {
            secondValueRetake = second.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Retake: " + secondValueRetake);
        assert (secondValueRetake == false);

        // Test 3: Immediate true and delayed false.
        c = new Circuit(CircuitNode.mk(NodeType.OR, CircuitNode.mk(false, Duration.ofSeconds(3)), CircuitNode.mk(true)));
        System.out.println("Solving 3...");
        CircuitValue third = solver.solve(c);
        Boolean thirdValue;
        try {
            thirdValue = third.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 3.: " + thirdValue);
        assert (thirdValue == false);
        Boolean thirdValueRetake;
        try {
            thirdValueRetake = third.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Retake: " + thirdValueRetake);
        assert (thirdValueRetake == false);

        System.out.println("End of TestOR");
    }
    
}
