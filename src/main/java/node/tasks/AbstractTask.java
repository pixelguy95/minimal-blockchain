package node.tasks;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractTask implements Runnable {

    AtomicBoolean keepAlive;

    public AbstractTask(AtomicBoolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void sleepWrapper(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}