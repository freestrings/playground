package fs.playground;

/**
 * docker run --name nginx --rm -v /home/han/Downloads:/usr/share/nginx/html:ro -p 8081:80 nginx:latest
 * watch -n 0.1 "netstat -an | grep 8081 | grep ESTABLISHED | wc -l"
 * <p>
 * HttpClient
 * <p>
 * warmup
 * warmup done
 * hc start 1
 * hc done  1 [1 sec, 1547 millisec]
 * hc start 2
 * hc done  2 [1 sec, 1347 millisec]
 * hc start 3
 * hc done  3 [0 sec, 962 millisec]
 * <p>
 * <p>
 * => 최대 약 8개 커넥션, 약 300 TIME_WAIT
 * <p>
 * Pooled HttpClient
 * <p>
 * warmup
 * warmup done
 * phc start 1
 * phc done  1 [1 sec, 1033 millisec]
 * phc start 2
 * phc done  2 [0 sec, 546 millisec]
 * phc start 3
 * phc done  3 [0 sec, 572 millisec]
 * <p>
 * <p>
 * => 최대 약 200개 커넥션, 380 TIME_WAIT
 * <p>
 * RestTemplate
 * <p>
 * warmup
 * warmup done
 * rt start 1
 * rt done  1 [1 sec, 1358 millisec]
 * rt start 2
 * rt done  2 [0 sec, 786 millisec]
 * rt start 3
 * rt done  3 [0 sec, 786 millisec]
 * <p>
 * <p>
 * => 최대 약 200 커넥션, 약 2000 TIME_WAIT
 * <p>
 * Pooled RestTemplate
 * <p>
 * warmup
 * warmup done
 * prt start 1
 * prt done  1 [1 sec, 1545 millisec]
 * prt start 2
 * prt done  2 [0 sec, 880 millisec]
 * prt start 3
 * prt done  3 [0 sec, 751 millisec]
 * <p>
 * <p>
 * => 최대 약 200개 커넥션,약 380 TIME_WAIT
 * <p>
 * WebClient
 * <p>
 * warmup
 * warmup done
 * wc start 1
 * wc done  1 [2 sec, 2186 millisec]
 * wc start 2
 * wc done  2 [1 sec, 1260 millisec]
 * wc start 3
 * wc done  3 [1 sec, 1116 millisec]
 * <p>
 * => 최대 500개 미만 커넥션, 약 460 TIME_WAIT
 */
public class TestHttpClient {

    public static final String URL = "http://localhost:8081/test.txt";


    public static void main(String... args) throws InterruptedException {
        TimeWatch watch = new TimeWatch();

        Testable testable = null;
        String cmd = args[0];

        if ("hc".equals(cmd)) {
            testable = new TestHttClientDefault();
        } else if ("phc".equals(cmd)) {
            testable = new TestPooledHttClient();
        } else if ("rt".equals(cmd)) {
            testable = new TestRestTemplateDefault();
        } else if ("prt".equals(cmd)) {
            testable = new TestPooledRestTemplate();
        } else if ("wc".equals(cmd)) {
            testable = new TestWebClient();
        } else {
            System.out.format("unknown command %s\n", cmd);
            System.exit(1);
        }

        System.out.println("warmup");
        testable.doTest(10, URL);
        System.out.println("warmup done");

        Thread.sleep(1000);

        for (int i = 1; i <= 3; i++) {
            System.out.format("%s start %d\n", cmd, i);
            watch.reset();
            testable.doTest(10000, URL);
            System.out.format("%s done  %d [%s]\n", cmd, i, watch.toSeconds());
        }
    }
}