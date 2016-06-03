package fs.lock.my;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ReentrantLockConditionBasic {

    public static void main(String... args) {
        int iterCount = 20;
        ShareBasic share = new ShareBasic(iterCount);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit((Runnable) () ->
                IntStream.range(0, iterCount).forEach(i -> share.doUp())
        );
        executorService.submit((Runnable) () -> {
            try {
                while (true) {
                    share.doDown();
                }
            } catch (InterruptedException e) {
                executorService.shutdown();
            }
        });
    }
}

class ShareBasic {

    int iterCount;
    Lock lock = new ReentrantLock();
    Condition condition;
    int interCount;

    // volatile: https://twitter.com/_freestrings/status/688963217784082432
    volatile boolean isUp;

    public ShareBasic(int iterCount) {
        this.iterCount = iterCount;
        condition = lock.newCondition();
    }

    void doUp() {
        lock.lock();
        try {
            while (!isUp) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isUp = false;
            System.out.println("↑");
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    void doDown() throws InterruptedException {
        if (interCount++ == iterCount) {
            throw new InterruptedException();
        }

        lock.lock();
        try {
            while (isUp) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isUp = true;
            System.out.print("↓");
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

}