package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestNOT {
    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();

        // Test 1: Immediate NOT.
        Circuit c = new Circuit(CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true)));
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
        assert (firstValue == false);
        Boolean firstValueRetake;
        try {
            firstValueRetake = first.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Retake: " + firstValueRetake);
        assert (firstValue == false);

        // Test 2: Sleepy NOT.
        c = new Circuit(CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false, Duration.ofSeconds(3))));
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
        assert (secondValue == true);
        Boolean secondValueRetake;
        try {
            secondValueRetake = second.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Retake: " + secondValueRetake);
        assert (secondValueRetake == true);

        System.out.println("End of TestNOT");
    }
    
}
