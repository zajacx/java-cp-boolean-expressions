package cp2024.test;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;
import cp2024.utils.Tester;

public class TestIF {

    public static void main(String[] args) {

        CircuitSolver solver = new ParallelCircuitSolver();
        Tester tester = new Tester();
        Circuit c;
/*
        // Test 1: [immediate true, immediate true, immediate false]
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)));
        CircuitValue first = solver.solve(c);
        Boolean firstValue;
        try {
            firstValue = first.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 2.: " + firstValue);
        tester.check(firstValue, true);

        // Test 2: [immediate false, immediate true, immediate false]
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)));
        CircuitValue second = solver.solve(c);
        Boolean secondValue;
        try {
            secondValue = second.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 2.: " + secondValue);
        tester.check(secondValue, false);

        // Test 3: [delayed true, immediate true, immediate false]
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)));
        CircuitValue third = solver.solve(c);
        Boolean thirdValue;
        try {
            thirdValue = third.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 3.: " + thirdValue);
        tester.check(thirdValue, true);

        // Test 4: [delayed false, immediate true, immediate false]
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false, Duration.ofSeconds(3)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)));
        CircuitValue fourth = solver.solve(c);
        Boolean fourthValue;
        try {
            fourthValue = fourth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 4.: " + fourthValue);
        tester.check(fourthValue, false);

        // Test 5: [delayed true, delayed true, immediate false]
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(false)));
        CircuitValue fifth = solver.solve(c);
        Boolean fifthValue;
        try {
            fifthValue = fifth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 5.: " + fifthValue);
        tester.check(fifthValue, true);

        // Test 6: [delayed false, delayed true, immediate false] TIME: 3 s
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false, Duration.ofSeconds(50)),
                        CircuitNode.mk(false, Duration.ofSeconds(3)),
                        CircuitNode.mk(false)));
        CircuitValue sixth = solver.solve(c);
        Boolean sixthValue;
        try {
            sixthValue = sixth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 6.: " + sixthValue);
        tester.check(sixthValue, false);

        // Test 7: [delayed true, delayed true, delayed false] TIME: 4 s
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true, Duration.ofSeconds(50)),
                        CircuitNode.mk(false, Duration.ofSeconds(3)),
                        CircuitNode.mk(false, Duration.ofSeconds(4))));
        CircuitValue seventh = solver.solve(c);
        Boolean seventhValue;
        try {
            seventhValue = seventh.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 7.: " + seventhValue);
        tester.check(seventhValue, false);
*/

        // Test 8: [delayed false, delayed true, delayed false] TIME: 5 s
        c = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false, Duration.ofSeconds(5)),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(false, Duration.ofSeconds(4))));
        CircuitValue eighth = solver.solve(c);
        Boolean eighthValue;
        try {
            eighthValue = eighth.getValue();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
        System.out.println("Solved 8.: " + eighthValue);
        tester.check(eighthValue, false);

        System.out.println("End of TestIF");

        try {
            System.out.println("Testing multiple getValue() calls: ");
            System.out.println(eighth.getValue());
            System.out.println(eighth.getValue());
            System.out.println(eighth.getValue());
            System.out.println(eighth.getValue());
            System.out.println(eighth.getValue());
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            return;
        }
    }
}
