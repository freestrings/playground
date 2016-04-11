package fs;

import com.netflix.hystrix.*;

public class HelloPrimaySecondaryCommand extends HystrixCommand<String> {

    private final int id;
    private final boolean usePrimary;
    private long sleep = 0;

    public HelloPrimaySecondaryCommand(int id, boolean usePrimary, long sleep) {
        this(id, usePrimary);
        this.sleep = sleep;
    }

    public HelloPrimaySecondaryCommand(int id, boolean usePrimary) {
        super(Setter
                .withGroupKey(
                        HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup")
                )
                .andCommandKey(
                        HystrixCommandKey.Factory.asKey("PrimarySecondaryCommandKey")
                )
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                .withExecutionIsolationStrategy(
                                        HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE
                                )
                )//
        );

        this.id = id;
        this.usePrimary = usePrimary;
    }

    @Override
    protected String run() throws Exception {
        return usePrimary ? new PrimaryCommand(id).execute() : new SecondaryCommand(id, sleep).execute();
    }

    private static class PrimaryCommand extends HystrixCommand<String> {

        private final int id;

        protected PrimaryCommand(int id) {
            super(Setter
                    .withGroupKey(
                            HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup")
                    )
                    .andCommandKey(HystrixCommandKey.Factory.asKey("PrimayCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("PrimaryCommand"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(600)
                    )
            );
            this.id = id;
        }

        @Override
        protected String run() throws Exception {
            return "responseFromPrimary-" + id;
        }
    }

    private static class SecondaryCommand extends HystrixCommand<String> {

        private final int id;
        private long sleep = 0;

        protected SecondaryCommand(int id, long sleep) {
            this(id);
            this.sleep = sleep;
        }

        protected SecondaryCommand(int id) {
            super(Setter
                    .withGroupKey(
                            HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup")
                    )
                    .andCommandKey(HystrixCommandKey.Factory.asKey("SecondaryCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("SecondaryCommand"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(100)
                    )
            );
            this.id = id;
        }

        @Override
        protected String run() throws Exception {
            if (this.sleep > 0) Thread.sleep(101);
            return "responseFromSecondary-" + id;
        }

        @Override
        protected String getFallback() {
            return "timeout-" + id;
        }
    }

}
