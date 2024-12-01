package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;
import cp2024.utils.Tester;

public class TestGT {

    public static void main(String[] args) throws InterruptedException {
        
        CircuitSolver solver = new ParallelCircuitSolver();
        Tester tester = new Tester();

        // Test 1: Immediate positive.
        Circuit c = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true)));
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
                CircuitNode.mk(NodeType.GT, 2,
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

        // Test 3: Edge case - threshold > number of inputs.
        c = new Circuit(
                CircuitNode.mk(NodeType.GT, 6,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true)));
        System.out.println("Solving 3...");
        CircuitValue third = solver.solve(c);
        Boolean thirdValue;
        try {
            thirdValue = third.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 3: " + thirdValue);
        tester.check(thirdValue, false);

        // Test 4: Sleepy positive.
        c = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true)));
        System.out.println("Solving 4...");
        CircuitValue fourth = solver.solve(c);
        Boolean fourthValue;
        try {
            fourthValue = fourth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 4: " + fourthValue);
        tester.check(fourthValue, true);

        // Test 5: Sleepy negative.
        c = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(false),
                        CircuitNode.mk(false, Duration.ofSeconds(3)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true)));
        System.out.println("Solving 5...");
        CircuitValue fifth = solver.solve(c);
        Boolean fifthValue;
        try {
            fifthValue = fifth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 5: " + fifthValue);
        tester.check(fifthValue, false);

        // Test 6: Got enough positive before checking all inputs.
        c = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true, Duration.ofSeconds(1)),
                        CircuitNode.mk(false, Duration.ofSeconds(1)),
                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(true, Duration.ofSeconds(5))));
        System.out.println("Solving 6...");
        CircuitValue sixth = solver.solve(c);
        Boolean sixthValue;
        try {
            sixthValue = sixth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 6: " + sixthValue);
        tester.check(sixthValue, true);

        // Test 7: Got enough negative before checking all inputs.
        c = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(false, Duration.ofSeconds(1)),
                        CircuitNode.mk(false, Duration.ofSeconds(1)),
                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                        CircuitNode.mk(false, Duration.ofSeconds(3)),
                        CircuitNode.mk(true, Duration.ofSeconds(5))));
        System.out.println("Solving 7...");
        CircuitValue seventh = solver.solve(c);
        Boolean seventhValue;
        try {
            seventhValue = seventh.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 7: " + seventhValue);
        tester.check(seventhValue, false);

        System.out.println("End of TestGT");
    }
    
}
