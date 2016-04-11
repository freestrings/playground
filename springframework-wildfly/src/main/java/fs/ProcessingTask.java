package fs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ProcessingTask extends TimerTask {

    private long reqId;
    private AtomicLong concurrentRequests;
    private DeferredResult<ProcessingStatus> deferredResult;
    private int processingTimeMs;

    public ProcessingTask(long reqId, AtomicLong concurrentRequests, int processingTimeMs, DeferredResult<ProcessingStatus> deferredResult) {
        this.reqId = reqId;
        this.concurrentRequests = concurrentRequests;
        this.processingTimeMs = processingTimeMs;
        this.deferredResult = deferredResult;
    }

    @Override
    public void run() {
        long concReqs = concurrentRequests.getAndDecrement();
        if (deferredResult.isSetOrExpired()) {
            log.warn("{}: Processing of non-blocking request #{} already expired", concReqs, reqId);
        } else {
            boolean deferredStatus = deferredResult.setResult(new ProcessingStatus("Ok", processingTimeMs));
            log.debug("{}: Processing of non-blocking request #{} done, deferredStatus = {}", concReqs, reqId, deferredStatus);
        }
    }
}
