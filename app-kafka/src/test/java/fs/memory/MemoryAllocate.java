package fs.memory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class MemoryAllocate {

    public static void main(String... args) {

        long availableMemory = 1000 * 1000 * 10;
        int poolableSize = 1000 * 1000;

        success(availableMemory, poolableSize);
//        fail(availableMemory, poolableSize);
//        successWithDeallocate(availableMemory, poolableSize);
    }

    /**
     * 가용량 만큼 생성 되었기 때문에 성공
     */
    private static void success(long availableMemory, int poolableSize) {
        BufferPool bufferPool = new BufferPool(availableMemory, poolableSize);
        IntStream.range(0, 10)
                .parallel()
                .forEach(i -> {
                    try {
                        bufferPool.allocate(1000);
                    } catch (TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 가용량을 초과했기 때문에 메모리 할당을 기다리다 실패
     */
    private static void fail(long availableMemory, int poolableSize) {
        BufferPool bufferPool = new BufferPool(availableMemory, poolableSize);
        IntStream.range(0, 11)
                .parallel()
                .forEach(i -> {
                    try {
                        bufferPool.allocate(1000);
                    } catch (TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 버퍼가 반환되며 할당을 기다리던 쓰레드가 버퍼를 할당받고 성공
     */
    private static void successWithDeallocate(long availableMemory, int poolableSize) {
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
        }, 2990);

        IntStream.range(0, 11)
                .parallel()
                .forEach(i -> {
                    try {
                        buffers.add(bufferPool.allocate(3000));
                    } catch (TimeoutException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}

/**
 *
 * # 왜 ByteBuffer.allocateDirect를 쓰지 않고 ByteBuffer.allocate를 쓰나?
 * - http://www.informit.com/articles/article.aspx?p=2133373&seqNum=12
 * <p>
 * 생명주기가 짧거나 자주 사용되지 않는 객체에는 다이렉트 버퍼를 사용하지 않아야 한다. 왜냐하면,
 * 다이렉트 버퍼는 OS 종속적인 네이티브 코드를 사용하기 때문에 힙기반 버퍼보다 생성과 메모리 반환 비용이 높고
 * 가비지 컬렉터의 영역 밖이라 메모리 누수가 있을 수 있다. 용량이 큰 다이렉트 버퍼를 빈번하게 할당하면
 * OutofMemorryError가 생길 수 있다.
 * <p>
 * <p>
 * <p>
 * # 왜 lock.unlock()가 finally에서 일괄처리 되지 않나?
 * 아래처럼 버퍼를 할당하고 lock을 풀어야 버퍼할당 까지 동기화 하는 정확한 코드 같은데,,
 * <p>
 * lock.lock()
 * ByteBuffer buffer = ByteBuffer.allocate(poolableSize);
 * lock.unlock();
 * return buffer;
 * <p>
 * 버퍼를 할당 전후로 unlock하는 간단한 테스트코드를 만들어보면,
 * 버퍼 할당전 unlock하는 코드는 OutOfMemoryError 에러가 발생한다.
 *
 * @see https://gist.github.com/freestrings/f252af60cb7a992ee2df0dfd7c39cfa0#file-lockingofbytebufferallocation-java
 */
class BufferPool {

    // 풀링된 메모리를 제외하고 사용할 수 있는 메모리
    long availableMemory;

    // 기본 Buffer 크기
    int poolableSize;

    ReentrantLock lock = new ReentrantLock();

    // deallocate 될 때 ByteBuffer를 쌓아둔다.
    Deque<ByteBuffer> free = new ArrayDeque<>();

    // allocate될 때 가용 메모리가 없으면 waters.addLast(..) 하고
    // 가용한 메모리가 확보될때 까지 기다린 후(현재 쓰레드를 블럭한 후) waiters.removeFirst()로 큐에서 제거 한다.
    //
    // 블럭된 쓰레드가 timeout이 나면 예외가 발생되기 때문에,
    // 이런 예외없이 블럭된 쓰레드가 깨어나려면
    // deallocate때 풀에 버퍼가 반납 되는 시점에 쓰레드를 깨워주거나
    // 이전 쓰레드가 메모리를 확보를 마치고 블럭된 쓰레드가 있으면 깨워준다.
    //
    // 두가지 모두 watiers.peekFrist().signal()로 큐헤더에 있는 쓰레드 하나만 깨우며,
    // allocate는 메시지를 보내는 쓰레드에서 deallocate는 응답처리 쓰레드에서 호출 한다.
    //
    // 참고로, Condition.await가 여러 쓰레드에서 호출되었을때,
    // await 시간이 지나면 쓰레드가 await 이후 로직을 수행하지만 쓰레드가 블럭된 순서대로 수행되지 않는다.
    //
    // @see https://gist.github.com/freestrings/f252af60cb7a992ee2df0dfd7c39cfa0#file-reentrantlockcondition-java
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
            ByteBuffer buffer = blockAwaitUntilAvailable(moreMemory, maxTimeToBlock);

            Condition removed = this.waiters.removeFirst();
            if (moreMemory != removed) {
                throw new IllegalStateException("블럭시간 - moreMemory.awaite(..) 동안 deallocate 되지 않은 경우");
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
    private ByteBuffer blockAwaitUntilAvailable(Condition moreMemory, long maxTimeToBlock) throws
            InterruptedException,
            TimeoutException {

        int accumulated = 0;
        ByteBuffer buffer = null;

        while (accumulated < this.poolableSize) {

            System.out.println("Start block..");
            if (!moreMemory.await(maxTimeToBlock, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("지정된 시간동안 메모리를 할당하지 못함");
            }

            if (accumulated == 0 && !this.free.isEmpty()) {
                // 그 사이 deallocate 되면서 버퍼가 풀에 반납되었을 수 있다.

                buffer = this.free.pollFirst();
                accumulated = poolableSize;
            } else {
                // 가용한 메모리 확보

                freeUp(poolableSize - accumulated);
                int got = (int) Math.min(poolableSize - accumulated, this.availableMemory);
                this.availableMemory -= got;
                accumulated += got;
            }
        }

        return buffer;
    }

    /**
     * 사용하고 있는 메모리와 풀에 있는 버퍼크기의 총합이 기본 버퍼크기보다 크다면 새로 버퍼를 생성 할 수 있다.
     *
     * @return
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

}