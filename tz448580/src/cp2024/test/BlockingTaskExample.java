package cp2024.test;

public class BlockingTaskExample {
    public static void main(String[] args) throws InterruptedException {
        // Create a task that blocks on a method throwing InterruptedException
        Runnable blockingTask = () -> {
            try {
                System.out.println(Thread.currentThread().getName() + " is starting.");
                // Simulate a blocking operation (e.g., Thread.sleep or wait)
                Thread.sleep(5000); // Blocks for 5 seconds
                System.out.println(Thread.currentThread().getName() + " completed successfully.");
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted!");
                // Optionally, re-interrupt the thread to preserve the interruption status
                Thread.currentThread().interrupt();
            }
        };

        // Start the thread that will execute the blocking task
        Thread workerThread = new Thread(blockingTask, "WorkerThread");
        workerThread.start();

        // Let the main thread sleep briefly to ensure WorkerThread starts
        Thread.sleep(2000);

        // Interrupt the worker thread
        System.out.println("Main thread is interrupting " + workerThread.getName());
        workerThread.interrupt();

        // Wait for the worker thread to finish
        workerThread.join();
        System.out.println("Main thread is done.");
    }
}

