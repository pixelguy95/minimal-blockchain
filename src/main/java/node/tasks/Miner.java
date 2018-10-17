package node.tasks;

import java.util.concurrent.atomic.AtomicBoolean;

public class Miner extends AbstractTask {
    public Miner(AtomicBoolean keepAlive) {
        super(keepAlive);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
