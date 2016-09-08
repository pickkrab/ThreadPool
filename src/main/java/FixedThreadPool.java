import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
    private final Object lock = new Object();
    private final List<Thread> workers;

    public FixedThreadPool(int threadCount) {
        workers = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            workers.add(new FixedThread("My name is " + i));
        }
    }

    private class FixedThread extends Thread {
        public FixedThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                Runnable task;
                synchronized (lock) {
                    while (tasks.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.out.println("thread was interrupted");
                        }
                    }
                    task = tasks.poll();
                }
                try {
                    task.run();
                } catch (Exception e) {
                    System.out.println("Can't run this task");
                }
            }
        }
    }

    @Override
    public void start() {
        workers.forEach(Thread::start);
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (lock) {
            tasks.add(runnable);
            lock.notify();
        }
    }
}