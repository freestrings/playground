package fs;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class HelloHystrixCommand extends HystrixCommand<String> {

    private final String name;
    private boolean print = false;

    public HelloHystrixCommand(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"));
        this.name = name;
    }

    public HelloHystrixCommand(String name, boolean print) {
        super(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"));
        this.name = name;
        this.print = print;
    }

    @Override
    protected String run() throws Exception {
        if(print) {
            System.out.format("Run - {%s}!\n", name);
        }
        return "Hello " + name + "!";
    }

}
