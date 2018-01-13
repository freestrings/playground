package fs.playground;

import java.util.concurrent.TimeUnit;

public class TimeWatch {
    long starts;

    public TimeWatch reset() {
        starts = System.nanoTime();
        return this;
    }

    public long time() {
        long ends = System.nanoTime();
        return ends - starts;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.NANOSECONDS);
    }

    public String toSeconds() {
        return String.format("%d sec, %d millisec", time(TimeUnit.SECONDS), time(TimeUnit.MILLISECONDS));
    }
}
