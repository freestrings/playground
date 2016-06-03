package fs.lock.my;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class LockingOfByteBufferAllocation {

    public static void main(String... args) {
        mallocTest(5000, "1".equals(args[0]));
    }

    private static void mallocTest(int iter, boolean isType1) {
        AtomicInteger counter = new AtomicInteger(iter);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        Allocate allocate = new Allocate(counter, errorRef);
        long time = System.currentTimeMillis();

        new Thread() {
            @Override
            public void run() {
                while (true) {

                    if (errorRef.get() != null) {
                        System.err.println("Woops!");
                        errorRef.get().printStackTrace();
                        break;
                    }

                    if (counter.get() == 0) {
                        System.out.println(System.currentTimeMillis() - time);
                        break;
                    }
                }
            }
        }.start();

        try {
            IntStream.range(0, iter)
                    .forEach(i -> new T(allocate, isType1).start());
        } catch (OutOfMemoryError e) {
            errorRef.set(e);
        }
    }
}

class T extends Thread {
    private final Allocate allocate;
    private final boolean isType1;

    T(Allocate allocate, boolean isType1) {
        this.allocate = allocate;
        this.isType1 = isType1;
    }

    @Override
    public void run() {
        if (isType1)
            allocate.mallocType1();
        else
            allocate.mallocType2();
    }
}

class Allocate {

    private final AtomicInteger counter;
    private final AtomicReference<Throwable> errorRef;
    ReentrantLock lock = new ReentrantLock();
    public static final int CAPACITY = 10000000; // 10MB

    Allocate(AtomicInteger counter, AtomicReference<Throwable> errorRef) {
        this.counter = counter;
        this.errorRef = errorRef;
    }

    ByteBuffer mallocType1() {
        lock.lock();
        try {
            return ByteBuffer.allocate(CAPACITY);
        } catch (OutOfMemoryError e) {
            errorRef.set(e);
            return null;
        } finally {
            lock.unlock();
            counter.decrementAndGet();
        }
    }

    ByteBuffer mallocType2() {
        lock.lock();
        try {
            lock.unlock();
            return ByteBuffer.allocate(CAPACITY);
        } catch (OutOfMemoryError e) {
            errorRef.set(e);
            return null;
        } finally {
            counter.decrementAndGet();
        }
    }
}