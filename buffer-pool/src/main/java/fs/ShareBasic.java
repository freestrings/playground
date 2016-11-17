package fs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

//http://free-strings.blogspot.kr/2016/06/producer-bufferpool.html
//https://gist.github.com/freestrings/f252af60cb7a992ee2df0dfd7c39cfa0
public class ShareBasic {

    int iterCount;
    Lock lock = new ReentrantLock();
    Condition condition;
    int interCount;

    // volatile: https://twitter.com/_freestrings/status/688963217784082432
    // CPU 캐시에서 읽지 않고 메인 메모리에서 값을 읽게함.
    volatile boolean isUp;

    public ShareBasic(int iterCount) {
        this.iterCount = iterCount;
        condition = lock.newCondition();
    }

    void doUp() {
        lock.lock();
        try {
            while (!isUp) { // 이미 down임
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


    public static void main(String... args) {
        int iterCount = 10;
        ShareBasic share = new ShareBasic(iterCount);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() ->
                IntStream.range(0, iterCount).forEach(i -> share.doUp())
        );
        executorService.submit(() -> {
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
