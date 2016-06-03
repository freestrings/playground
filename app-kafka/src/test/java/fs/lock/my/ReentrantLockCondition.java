package fs.lock.my;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockCondition {

    public static void main(String... args) {
        Share share = new Share();
        new AwaitTestThread("A", share, "").start();
        new AwaitTestThread("B", share, "\t\t\t\t").start();

        // signal을 호출 하지 않으면 await에 지정된 시간이 지난 후
        // 쓰레드가 await 이후 로직을 수행하지만, 블록된 쓰레드 순서대로 실행되지 않는다
        // timer.schedule(..)을 제거해 보면 확인 할 수 있다.
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

class Share {
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
}

