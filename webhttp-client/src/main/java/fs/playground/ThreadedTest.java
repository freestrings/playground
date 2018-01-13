package fs.playground;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadedTest {

    public static void run(int loop, Runnable runnable) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 0; i < loop; i++) {
            executorService.execute(runnable);
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
