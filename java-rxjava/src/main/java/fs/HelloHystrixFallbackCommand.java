package fs;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class HelloHystrixFallbackCommand extends HystrixCommand<String> {

    private final String name;

    protected HelloHystrixFallbackCommand(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        throw new RuntimeException("Alwyas Fail");
    }

    @Override
    protected String getFallback() {
        return "Woops " + this.name;
    }
}
