package timeout;

public class TimeoutThread extends Thread{
    private final long timeout;
    private final Runnable callback;

    public TimeoutThread(long timeout, Runnable callback) {
        this.timeout = timeout;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timeout);
            callback.run();
        } catch (InterruptedException e) {
            // Thread was interrupted, do nothing
        }
    }
}
