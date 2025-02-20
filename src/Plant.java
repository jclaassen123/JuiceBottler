/*
Filename: Plant.java
Authors: Jace Claassen and Nate Williams
Description: This class is a simulation of plants and workers making orange juice.
Through data and task parallelization with threads there will be multiple oranges being worked
on at the same time. Each thread is responsible for different states of the orange juice making
process.

ChatGPT helped with parts of this code: Of the new code Jace Claassen wrote roughly 70% of it.
*/

public class Plant implements Runnable {
    public static final long PROCESSING_TIME = 5 * 1000; // Total processing time before stopping
    private static final int NUM_PLANTS = 2; // Number of plant instances
    private static final int WORKERS_PER_PLANT = 4; // Workers per plant

    private final Thread thread;
    private final int plantId;
    private volatile boolean timeToWork; // Flag to control plant operation

    //ChatGPT helped me understand and implement parts of the input/output BlockingMailbox arrays
    private final BlockingMailbox[] inputMailboxes; // Mailboxes for worker input
    private final BlockingMailbox[] outputMailboxes; // Mailboxes for worker output
    private int orangesProvided; // Count of provided oranges
    private int orangesProcessed; // Count of processed oranges

    public Plant(int plantId) {
        this.plantId = plantId;
        this.thread = new Thread(this, "Plant-" + plantId);
        this.timeToWork = true;
        this.inputMailboxes = new BlockingMailbox[WORKERS_PER_PLANT];
        this.outputMailboxes = new BlockingMailbox[WORKERS_PER_PLANT];

        // Initialize mailboxes for communication between workers
        for (int i = 0; i < WORKERS_PER_PLANT; i++) {
            inputMailboxes[i] = new BlockingMailbox();
            outputMailboxes[i] = new BlockingMailbox();
        }
    }

    /**
     * Starts the plant processing by starting the plant thread.
     */
    public void startPlant() {
        System.out.println("Starting Plant " + plantId);
        thread.start(); // Start plant processing
    }

    /**
     * Stops the plant and signals the workers to stop.
     */
    public void stopPlant() {
        System.out.println("Stopping Plant " + plantId);
        timeToWork = false; // Signal plant to stop
        for (BlockingMailbox mailbox : inputMailboxes) {
            mailbox.signalStop(); // Notify workers to stop waiting
        }
    }

    /**
     * Waits for the plant thread to finish execution.
     */
    public void waitToStop() {
        try {
            thread.join(); // Wait for plant thread to finish
        } catch (InterruptedException e) {
            System.err.println("Error stopping plant " + plantId);
        }
    }

    /**
     * Runs the plant processing logic, including creating worker threads and providing oranges for processing.
     */
    @Override
    public void run() {
        System.out.println("Plant " + plantId + " started.");

        // Create worker threads
        Thread[] workers = new Thread[WORKERS_PER_PLANT];
        for (int i = 0; i < WORKERS_PER_PLANT; i++) {
            int workerId = i;
            workers[i] = new Thread(() -> processOranges(workerId), "Plant-" + plantId + "-Worker-" + workerId);
            workers[i].start();
            System.out.println("Worker " + workerId + " started in Plant " + plantId);
        }

        // Provide oranges while the plant is running
        while (timeToWork) {
            Orange orange = new Orange();
            System.out.println("Plant " + plantId + " provided a new orange.");
            inputMailboxes[0].put(orange); // Send orange to first worker
            orangesProvided++;
        }

        // Wait for all worker threads to finish
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                System.err.println("Error stopping worker thread in Plant " + plantId);
            }
        }

        System.out.println("Plant " + plantId + " stopped.");
    }

    /**
     * Processes oranges by passing them through various stages using worker threads.
     * Each worker handles a stage in the processing pipeline.
     *
     * @param workerId The worker's ID to manage stages and mailboxes.
     */
    private void processOranges(int workerId) {
        while (timeToWork || !inputMailboxes[workerId].isEmpty()) {
            Orange orange = inputMailboxes[workerId].get(); // Get orange from previous stage
            if (orange == null) continue; // Skip if no orange available

            System.out.println("Worker " + workerId + " in Plant " + plantId + " processing orange at state: " + orange.getState());

            if (orange.getState() != Orange.State.Bottled) {
                orange.runProcess(); // Process orange to next state
            } else {
                orangesProcessed++; // Count processed oranges
            }

            // Pass orange to the next worker if applicable
            if (workerId < WORKERS_PER_PLANT - 1) {
                outputMailboxes[workerId].put(orange);
                inputMailboxes[workerId + 1].put(outputMailboxes[workerId].get());
            }
        }
    }

    /**
     * Returns the total number of oranges provided by the plant.
     *
     * @return The number of provided oranges.
     */
    public int getProvidedOranges() {
        return orangesProvided;
    }

    /**
     * Returns the total number of oranges processed by the plant.
     *
     * @return The number of processed oranges.
     */
    public int getProcessedOranges() {
        return orangesProcessed;
    }

    /**
     * Returns the number of bottled oranges (3 oranges per bottle).
     *
     * @return The number of bottles created.
     */
    public int getBottles() {
        return orangesProcessed / 3; // Bottled oranges count
    }

    /**
     * Returns the total number of wasted oranges (provided - processed).
     *
     * @return The number of wasted oranges.
     */
    public int getWaste() {
        int waste = 0;
        waste = orangesProvided - orangesProcessed; // Calculate waste
        return waste;
    }

    /**
     * Main method to run the plant simulation for a set processing time and then stop all plants.
     */
    public static void main(String[] args) {
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i + 1);
            plants[i].startPlant(); // Start each plant
        }

        try {
            Thread.sleep(PROCESSING_TIME); // Run for specified time
        } catch (InterruptedException e) {
            System.err.println("Plant malfunction");
        }

        // Stop all plants
        for (Plant p : plants) {
            p.stopPlant();
        }
        for (Plant p : plants) {
            p.waitToStop();
        }

        // Summarize results
        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles + ", wasted " + totalWasted + " oranges");
    }
}
