package fs;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class HelloHystrixCacheCommand extends HystrixCommand<Boolean> {

    private final int value;

    public HelloHystrixCacheCommand(int value) {
        super(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"));
        this.value = value;
    }

    @Override
    protected Boolean run() throws Exception {
        return value == 0 || value % 2 == 0;
    }

    @Override
    protected String getCacheKey() {
        return String.valueOf(value);
    }

}
