package fs;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class BufferPool {

    // 풀링된 메모리를 제외하고 사용할 수 있는 메모리
    long availableMemory;

    // 기본 Buffer 크기
    int poolableSize;

    ReentrantLock lock = new ReentrantLock();

    // deallocate 될 때 ByteBuffer를 쌓아둔다.
    Deque<ByteBuffer> free = new ArrayDeque<>();

    // 메모리를 할당받으려고 대기중인 쓰레드를 깨우기 위한 Condition을 모아둔다.
    Deque<Condition> waiters = new ArrayDeque<>();

    BufferPool(long availableMemory, int poolableSize) {
        this.availableMemory = availableMemory;
        this.poolableSize = poolableSize;
    }

    ByteBuffer allocate(long maxTimeToBlock) throws
            TimeoutException,
            InterruptedException {

        this.lock.lock();

        try {
            // 1. 풀링된 버퍼가 있으면 꺼내 쓴다
            if (!this.free.isEmpty()) {
                return this.free.pollFirst();
            }

            // 2. 메모리를 할당 할 수 있으면
            // 풀링된 버퍼는 꺼내썼기 때문에 새로 ByteBuffer를 생성한다.
            if (canBeNewlyAllocated()) {
                freeUp(this.poolableSize);
                this.availableMemory -= poolableSize;

                // !
                lock.unlock();
                return ByteBuffer.allocate(poolableSize);
            }

            Condition moreMemory = this.lock.newCondition();
            waiters.addLast(moreMemory);

            // 3. 가용한 메모리가 없기 때문에 풀에 버퍼가 반납되거나
            // 이전 쓰레드에서 메모리를 확보해 주기를 기다린다.
            ByteBuffer buffer = blockAwaitUntilAvailable(
                    moreMemory,
                    maxTimeToBlock
            );

            Condition removed = this.waiters.removeFirst();
            if (moreMemory != removed) {
                throw new IllegalStateException(
                        "블럭시간 - moreMemory.awaite(..) 동안 deallocate 되지 않은 경우"
                );
            }

            // deallocate 시점이 아니더라도 가용메모리가 확보 되었거나
            // 그 사이 풀에 반납된 버퍼가 있을 수 있으니 바로 다음 쓰레드를 깨워준다.
            if (this.availableMemory > 0 || !this.free.isEmpty()) {
                if (!this.waiters.isEmpty())
                    this.waiters.peekFirst().signal();
            }


            lock.unlock();

            if (buffer == null)
                return ByteBuffer.allocate(poolableSize);
            else
                return buffer;

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    void deallocate(ByteBuffer buffer) {
        lock.lock();
        try {
            if (this.poolableSize == buffer.capacity()) {
                buffer.clear();
                this.free.add(buffer);
            } else {
                this.availableMemory += this.poolableSize;
            }
            System.out.println("Waiters: " + this.waiters.size());
            Condition moreMemory = this.waiters.peekFirst();
            if (moreMemory != null)
                moreMemory.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 가용한 메모리와 풀링된 버퍼를 모두 합해도 poolableSize 만큼 되지 않는 경우.
     * <p>
     * 현재 쓰레드를 블럭한뒤 deallocate 로 풀에 버퍼가 반납 되는 시점이나,
     * 이전 쓰레드가 버퍼 할당을 마치는 시점에 poolableSize가 확보 되었는지 체크한다.
     */
    private ByteBuffer blockAwaitUntilAvailable(
            Condition moreMemory,
            long maxTimeToBlock
    ) throws InterruptedException, TimeoutException {

        int accumulated = 0;
        ByteBuffer buffer = null;

        while (accumulated < this.poolableSize) {

            System.out.println("Start block..");
            if (!moreMemory.await(
                    maxTimeToBlock,
                    TimeUnit.MILLISECONDS)
                    ) {
                throw new TimeoutException(
                        "지정된 시간동안 메모리를 할당하지 못함"
                );
            }

            if (accumulated == 0 && !this.free.isEmpty()) {
                // 그 사이 deallocate 되면서 버퍼가 풀에 반납되었을 수 있다.

                buffer = this.free.pollFirst();
                accumulated = poolableSize;
            } else {
                // 가용한 메모리 확보

                freeUp(poolableSize - accumulated);
                int got = (int) Math.min(
                        poolableSize - accumulated,
                        this.availableMemory
                );

                this.availableMemory -= got;
                accumulated += got;
            }
        }

        return buffer;
    }

    /**
     * 사용하고 있는 메모리와 풀에 있는 버퍼크기의 총합이 기본 버퍼크기보다 크다면
     * 새로 버퍼를 생성 할 수 있다.
     */
    private boolean canBeNewlyAllocated() {
        return this.availableMemory + this.free.size() * this.poolableSize >= this.poolableSize;
    }

    /**
     * 할당하려는 크기보다 작으면 pooling된 버퍼를 해제해서 가용한 메모리를 확보한다.
     */
    void freeUp(int size) {
        while (!this.free.isEmpty() && this.availableMemory < size) {
            this.availableMemory += this.free.pollLast().capacity();
        }
    }

    public static void main(String... args) {
//        case1();
//        case2();
        case3();
    }

    private static void case1() {
        long availableMemory = 1000 * 1000 * 10; // 10MB
        int poolableSize = 1000 * 1000; // 1MB
        BufferPool bufferPool = new BufferPool(availableMemory, poolableSize);
        IntStream.range(0, 10) // 10개
                .parallel() // 쓰레드
                .forEach(i -> {
                    try {
                        bufferPool.allocate(1000);
                    } catch (TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void case2() {
        long availableMemory = 1000 * 1000 * 10; // 10MB
        int poolableSize = 1000 * 1000; // 1MB
        BufferPool bufferPool = new BufferPool(availableMemory, poolableSize);
        IntStream.range(0, 11) // 11개
                .parallel() // 쓰레드
                .forEach(i -> {
                    try {
                        bufferPool.allocate(1000);
                    } catch (TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void case3() {
        long availableMemory = 1000 * 1000 * 10; // 10 MB
        int poolableSize = 1000 * 1000; // 1MB
        BufferPool bufferPool = new BufferPool(availableMemory, poolableSize);
        List<ByteBuffer> buffers = new ArrayList<>();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Start deallocate");
                bufferPool.deallocate(buffers.get(0));
                timer.cancel();
            }
        }, 2990); // 2.99초에 반납

        IntStream.range(0, 11) // 11개
                .parallel() // 쓰레드
                .forEach(i -> {
                    try {
                        buffers.add(bufferPool.allocate(3000)); // 3초 기다림
                    } catch (TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}
