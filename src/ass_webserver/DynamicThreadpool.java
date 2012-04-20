/*
 */
package ass_webserver;

import java.util.LinkedList;

public class DynamicThreadpool {

    private final int poolsize;
    private LinkedList<PoolWorker> pool;
    private final LinkedList<Runnable> queue;
    private boolean quitAuxiliaryThreads;

    public DynamicThreadpool(int poolsize) {
        this.poolsize = poolsize;
        pool = new LinkedList<PoolWorker>();
        queue = new LinkedList<Runnable>();
        quitAuxiliaryThreads = false;

        for (int i = 0; i < poolsize; i++) {
            PoolWorker p = new PoolWorker();
            pool.add(p);
            p.start();
        }
    }

    private synchronized void setQuitAuxiliaryThreads(boolean b) {
        quitAuxiliaryThreads = b;
    }

    private synchronized boolean quitAuxiliaryThreads() {
        return quitAuxiliaryThreads;
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
            setQuitAuxiliaryThreads(true);
        }
        while (!queue.isEmpty()) {
            setQuitAuxiliaryThreads(false);
            PoolWorker p = new PoolWorker();
            pool.add(p);
            p.start();
            synchronized (queue) {
                queue.notify();
            }
        }
    }

    private class PoolWorker extends Thread {

        @Override
        public void run() {
            Runnable r;
            try {
                while (true) {
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            queue.wait();
                        }
                        r = queue.removeFirst();
                    }
                    r.run();
                    if (quitAuxiliaryThreads() == true && pool.size() > poolsize) {
                        break;
                    }
                }

            } catch (InterruptedException ignored) {
            } catch (RuntimeException e) {
                System.out.println("thread lost!");
            }
        }
    }
}