package fs;

import com.sun.management.UnixOperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@Slf4j
public class AyncVsSyncController {

    private static Timer timer = new Timer();
    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private static final AtomicLong lastRequestId = new AtomicLong(0);
    private static final AtomicLong concurrentRequests = new AtomicLong(0);
    private static long maxConcurrentRequests = 0;

//    @Value("${statistics.requestsPerLog}")
//    private int STAT_REQS_PER_LOG;

    @RequestMapping("/process-blocking")
    public ProcessingStatus blockingProcessing(
            @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
            @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {


        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();

        updateStatistics(reqId, concReqs);

        int processingTimeMs = calculateProcessingTime(minMs, maxMs);

        log.debug("{}: Start blocking request #{}, processing time: {} ms.", concReqs, reqId, processingTimeMs);

        try {
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
        } finally {
            concurrentRequests.decrementAndGet();
            log.debug("{}: Processing of blocking request #{} is done", concReqs, reqId);
        }

        return new ProcessingStatus("Ok", processingTimeMs);
    }

    @RequestMapping("/process-non-blocking")
    public DeferredResult<ProcessingStatus> nonBlockingProcessing(
            @RequestParam(value = "minMs", required = false, defaultValue = "0") int minMs,
            @RequestParam(value = "maxMs", required = false, defaultValue = "0") int maxMs) {

        long reqId = lastRequestId.getAndIncrement();
        long concReqs = concurrentRequests.getAndIncrement();

        updateStatistics(reqId, concReqs);

        int processingTimeMs = calculateProcessingTime(minMs, maxMs);

        log.debug("{}: Start non-blocking request #{}, processing time: {} ms.", concReqs, reqId, processingTimeMs);

        // Create the deferredResult and initiate a callback object, task, with it
        DeferredResult<ProcessingStatus> deferredResult = new DeferredResult<>();
        ProcessingTask task = new ProcessingTask(reqId, concurrentRequests, processingTimeMs, deferredResult);

        // Schedule the task for asynch completion in the future
        timer.schedule(task, processingTimeMs);

        log.debug("{}: Processing of non-blocking request #{} leave the request thread", concReqs, reqId);

        // Return to let go of the precious thread we are holding on to...
        return deferredResult;
    }

    private void updateStatistics(long reqId, long concReqs) {
//        if (concReqs > maxConcurrentRequests) {
//            maxConcurrentRequests = concReqs;
//        }
//
//        if (reqId % STAT_REQS_PER_LOG == 0 && reqId > 0) {
//            Object openFiles = "UNKNOWN";
//            if (os instanceof UnixOperatingSystemMXBean) {
//                openFiles = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
//            }
//            log.info("Statistics: noOfReqs: {}, maxConcReqs: {}, openFiles: {}", reqId, maxConcurrentRequests, openFiles);
//        }
    }

    private int calculateProcessingTime(int minMs, int maxMs) {
        if (maxMs < minMs) maxMs = minMs;
        int processingTimeMs = minMs + (int) (Math.random() * (maxMs - minMs));
        return processingTimeMs;
    }

}
