/*
 */
package ass_webserver;

import java.util.LinkedList;

public class StaticThreadpool {

    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    public StaticThreadpool(int nThreads) {
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {

        @Override
        public void run() {
            Runnable r;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {
                            System.out.println(ignored);
                        }
                    }
                    r = queue.removeFirst();
                }
              
                try {
                    r.run();
                } catch (RuntimeException e) {
                    System.out.println("thread lost!");
                }
            }
        }
    }
}