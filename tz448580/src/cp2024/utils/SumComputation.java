package cp2024.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SumComputation {

    private static class Tree {
        private TreeNode root;

        public Tree() {
            this.root = null;
        }

        public void insert(int value) {
            root = insertRec(root, value);
        }

        private TreeNode insertRec(TreeNode node, int value) {
            if (node == null) {
                return new TreeNode(value);
            }
            if (value < node.value) {
                node.left = insertRec(node.left, value);
            } else if (value > node.value) {
                node.right = insertRec(node.right, value);
            }
            return node;
        }

        /*
        public void printInorder() {
            printInorderRec(root);
        }

        private void printInorderRec(TreeNode node) {
            if (node != null) {
                printInorderRec(node.left);
                System.out.println(node.value);
                printInorderRec(node.right);
            }
        }
        */

    }

    private static class TreeNode {
        private int value;
        private TreeNode left, right;

        TreeNode(int value) {
            this.value = value;
            left = null;
            right = null;
        }
    }

    private static class TreeBuilder {
        private final int[] values;

        public TreeBuilder(int[] values) {
            this.values = values;
        }

        public Tree build() {
            Tree tree = new Tree();
            for (int value : values) {
                tree.insert(value);
            }
            return tree;
        }
    }

    public static int[] generateAndPermute(int n) {
        // Create a list to hold the numbers from 1 to n
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            numbers.add(i);
        }

        // Shuffle the list randomly
        Collections.shuffle(numbers);

        // Convert the list to an array
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = numbers.get(i);
        }

        return result;
    }

    private static class Computation extends RecursiveTask<Integer> {

        private final TreeNode node;

        public Computation(TreeNode node) {
            this.node = node;
        }

        @Override
        protected Integer compute() {
            if (node == null) {
                return 0;
            } else {
                Computation left = new Computation(node.left);
                Computation right = new Computation(node.right);
                right.fork();  // Fork the right subtree computation
                int leftResult = left.compute();  // Compute the left subtree in the current thread
                int rightResult = right.join();  // Wait for the right subtree result
                return node.value + leftResult + rightResult;  // Sum up the values
            }
        }

    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        int[] values = generateAndPermute(100);
        TreeBuilder builder = new TreeBuilder(values);
        Tree tree = builder.build();
        // tree.printInorder();
        try {
            RecursiveTask<Integer> computation = new Computation(tree.root);
            int result = pool.invoke(computation);
            System.out.println(result);
        } finally {
            pool.shutdown();
        }
    }

}

