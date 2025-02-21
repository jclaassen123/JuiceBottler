/*
Filename: Orange.java
Author: Nate Williams
Description: This class defines the different states of an orange while being processed.
Utilized by the Plant.java class through task and data parallelization.
 */

public class Orange {
    /**
     * Enum representing the various states of orange processing.
     * Each state has a predefined time to complete in milliseconds.
     */
    public enum State {
        Fetched(15),    // Initial state when an orange is fetched
        Peeled(38),     // Orange is peeled
        Squeezed(29),   // Juice is extracted from the orange
        Bottled(17),    // Juice is bottled
        Processed(1);   // Final state indicating completion

        private static final int finalIndex = State.values().length - 1;

        final int timeToComplete; // Time required to complete this processing stage

        /**
         * Constructor to initialize state with its processing time.
         *
         * @param timeToComplete Time required to complete this state.
         */
        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        /**
         * Gets the next state in the processing sequence.
         *
         * @return The next processing state.
         * @throws IllegalStateException If already in the final state.
         */
        State getNext() {
            int currIndex = this.ordinal();
            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }
            return State.values()[currIndex + 1];
        }
    }

    private State state; // Current state of the orange

    /**
     * Constructor initializes the orange in the 'Fetched' state
     * and immediately starts processing.
     */
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    /**
     * Retrieves the current state of the orange.
     *
     * @return The current processing state.
     */
    public State getState() {
        return state;
    }

    /**
     * Advances the orange to the next processing stage.
     * Throws an exception if the orange is already fully processed.
     *
     * @throws IllegalStateException If the orange is already processed.
     */
    public void runProcess() {
        if (state == State.Processed) {
            throw new IllegalStateException("This orange has already been processed");
        }
        doWork();
        state = state.getNext();
    }

    /**
     * Simulates the work required for the current processing stage
     * by making the thread sleep for the required time.
     */
    private void doWork() {
        try {
            Thread.sleep(state.timeToComplete);
        } catch (InterruptedException e) {
            System.err.println("Incomplete orange processing, juice may be bad");
        }
    }
}
