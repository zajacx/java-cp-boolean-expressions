package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class TestLEAF {
    public static void main(String[] args) throws InterruptedException {
        
        CircuitSolver solver = new ParallelCircuitSolver();

        // Test 1: Immediate leaf.
        Circuit c = new Circuit(CircuitNode.mk(true));
        System.out.println("Solving 1...");
        CircuitValue first = solver.solve(c);
        Boolean firstValue = first.getValue();
        System.out.println("Solved 1: " + firstValue);
        assert (firstValue == true);
        Boolean firstValueRetake = first.getValue();
        System.out.println("Retake: " + firstValueRetake);
        assert (firstValue == true);

        // Test 2: Sleepy leaf.
        c = new Circuit(CircuitNode.mk(false, Duration.ofSeconds(3)));
        System.out.println("Solving 2...");
        CircuitValue second = solver.solve(c);
        Boolean secondValue = second.getValue();
        System.out.println("Solved 2.: " + secondValue);
        assert (secondValue == false);
        Boolean secondValueRetake = second.getValue();
        System.out.println("Retake: " + secondValueRetake);
        assert (secondValueRetake == false);

        System.out.println("End of TestLEAF");
    }
}
