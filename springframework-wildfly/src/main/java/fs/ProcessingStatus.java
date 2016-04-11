package fs;

import lombok.Getter;

@Getter
public class ProcessingStatus {

    private final String status;

    private final int processingTimeMs;

    public ProcessingStatus() {
        status = "UNKNOWN";
        processingTimeMs = -1;
    }

    public ProcessingStatus(String status, int processingTimeMs) {
        this.status = status;
        this.processingTimeMs = processingTimeMs;
    }
}
