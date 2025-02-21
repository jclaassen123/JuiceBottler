/*
Filename: BlockingMailbox.java
Authors: Jace Claassen and Nate Williams
Description: This class is responsible for proper coordination between threads
working on the oranges.
*/
public class BlockingMailbox {
    private Orange orange;
    private volatile boolean stopped = false;

    /**
     * Constructor initializes an empty mailbox.
     */
    public BlockingMailbox() {
        orange = null;
    }

    /**
     * Puts an orange into the mailbox if it is empty.
     * If the mailbox is full, the thread waits until it is empty.
     * If stopped, it prevents adding new oranges.
     *
     * @param o The orange to be placed in the mailbox.
     */
    public synchronized void put(Orange o) {
        while (!isEmpty() && !stopped) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        if (stopped) return; // Prevent putting oranges after stopping

        orange = o;
        notifyAll();
    }

    /**
     * Retrieves and removes the orange from the mailbox if available.
     * If the mailbox is empty, the thread waits until an orange is available.
     * If stopped and empty, returns null.
     *
     * @return The retrieved orange or null if stopped and empty.
     */
    public synchronized Orange get() {
        while (isEmpty() && !stopped) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        if (stopped && isEmpty()) return null; // Return null if stopped and empty

        Orange ret = orange;
        orange = null;
        notifyAll();
        return ret;
    }

    /**
     * Checks if the mailbox is empty.
     *
     * @return true if empty, false otherwise.
     */
    public synchronized boolean isEmpty() {
        return orange == null;
    }

    /**
     * Signals all waiting threads to stop execution.
     * Prevents new oranges from being added and allows waiting threads to exit.
     */
    public synchronized void signalStop() {
        stopped = true;
        notifyAll(); // Wake up any waiting threads
    }
}