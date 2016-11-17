package fs;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Share {

    ReentrantLock lock = new ReentrantLock();
    Deque<Condition> waiters = new ArrayDeque<>();
    AtomicInteger counter = new AtomicInteger(0);

    public void awaitTest(String name, String prefix) {
        lock.lock();
        try {
            int i = counter.incrementAndGet();
            Condition condition = lock.newCondition();
            waiters.addLast(condition);

            System.out.println(prefix + name + i);

            try {
                condition.await(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println("###" + e.getMessage());
            }

            System.out.println(prefix + "\t" + name + i);

            Condition condition1 = waiters.removeFirst();
            if (condition != condition1) {
                throw new IllegalStateException(Thread.currentThread().getName() + ": Wooops");
            }

        } finally {
            lock.unlock();
        }
    }

    public void signalTest() {
        lock.lock();
        try {
            Condition condition = waiters.peekFirst();
            if (condition != null) {
                condition.signal();
            }
        } finally {
            lock.unlock();
        }

    }

    public static void main(String... args) {
        Share share = new Share();
        new AwaitTestThread("A", share, "").start();
        new AwaitTestThread("B", share, "\t\t\t\t").start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                share.signalTest();
            }
        }, 100);
    }
}

class AwaitTestThread extends Thread {

    private final Share share;
    private final String name;
    private final String prefix;

    public AwaitTestThread(String name, Share share, String prefix) {
        super(name);
        this.name = name;
        this.share = share;
        this.prefix = prefix;
    }

    @Override
    public void run() {
        while (true) {
            this.share.awaitTest(this.name, this.prefix);
        }
    }
}

