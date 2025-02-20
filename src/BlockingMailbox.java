/*
Filename: BlockingMailbox.java
Authors: Jace Claassen and Nate Williams
Description: This class is responsible for proper coordination between threads
working on the oranges.
*/
public class BlockingMailbox {
    private Orange orange;
    private volatile boolean stopped = false;

    public BlockingMailbox() {
        orange = null;
    }

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

    public synchronized boolean isEmpty() {
        return orange == null;
    }

    /*
    ChatGPT helped me add this method as a way to stop the threads from running when my
    program was completed. It prevents new oranges from being added to the mailboxes and
    allows threads to exit the waiting states once the simulation is complete. Otherwise
    there was an infinite loop that was never completed in the main method.
     */
    public synchronized void signalStop() {
        stopped = true;
        notifyAll(); // Wake up any waiting threads
    }
}