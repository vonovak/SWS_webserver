/*
 */
package ass_webserver;

import java.util.LinkedList;

public class StaticThreadpool {

    private final int numOfThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    public StaticThreadpool(int numOfThreads) {
        this.numOfThreads = numOfThreads;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[numOfThreads];

        for (int i = 0; i < numOfThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notifyAll();
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
                }

            } catch (InterruptedException ignored) {
            } catch (RuntimeException e) {
                System.out.println("thread lost!");
            }

        }
    }
}