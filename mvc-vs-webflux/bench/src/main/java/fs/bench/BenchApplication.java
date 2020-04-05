package fs.bench;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;

@SpringBootApplication
public class BenchApplication {

    public static void main(String[] args) throws InterruptedException {
        String url = args[0];

        RestTemplate restTemplate = new RestTemplate();

        final int nThreads = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(nThreads);
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);

        long time = System.currentTimeMillis();

        for (int i = 0; i < nThreads; i++) {
            int index = i;

            executorService.submit(() -> {
                // 동접의 의미로 100개 요청을 한번에 보낸다.
                // 요청 쓰레드가 100개 되기 전까지 아래 라인은 실행되지 않는다
                cyclicBarrier.await();
                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                            "http://" + url + "?v=" + index,
                            HttpMethod.GET,
                            new HttpEntity<String>(new HttpHeaders()),
                            String.class
                    );
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        System.out.println("error");
                    }
                } finally {
                    countDownLatch.countDown();
                }
                return null;
            });
        }

        while (true) {
            if (countDownLatch.getCount() == 0) {
                System.out.println("전체 처리시간: " + (System.currentTimeMillis() - time) + "ms");
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.NANOSECONDS);
                break;
            }
            System.out.println("처리중: " + (countDownLatch.getCount() + "/" + nThreads));
            Thread.sleep(5000);
        }
    }

}

