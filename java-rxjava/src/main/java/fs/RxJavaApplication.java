package fs;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import fs.async.RestCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import rx.Observer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@SpringBootApplication
public class RxJavaApplication implements ApplicationListener<ApplicationReadyEvent> {

    public static void main(String... args) {
        new SpringApplicationBuilder(RxJavaApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .build()
                .run(args)
                .close()
        ;
    }

    @Autowired
    private RestCall restCall;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            hystrixCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hystrixObserverCommand();
        hystrixCacheCommand();
        try {
            hystrixCollapseCommand();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        hystrixObservableErrorCommand();
        hystrixPrimarySecondaryCommand();
        streamAsyc();
    }

    private void streamAsyc() {
        long time = System.currentTimeMillis();
        List<Integer> lists = Arrays.asList(1, 2);
        lists.forEach(x -> {
            String call = restCall.call(String.valueOf(x)).getBody();
            System.out.format("%s : %s\n", x, call);
        });
        System.out.println(System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        lists.stream().parallel()
                .map(x -> restCall.call(String.valueOf(x)).getBody())
                .collect(Collectors.toList())
                .forEach(System.out::println);
        System.out.println(System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        lists.parallelStream()
                .map(x -> restCall.call(String.valueOf(x)).getBody())
                .collect(Collectors.toList())
                .forEach(System.out::println);
        System.out.println(System.currentTimeMillis() - time);
    }

    private void hystrixPrimarySecondaryCommand() {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Assert.isTrue("responseFromPrimary-20".equals(new HelloPrimaySecondaryCommand(20, true).execute()));
            Assert.isTrue("responseFromSecondary-21".equals(new HelloPrimaySecondaryCommand(21, false).execute()));
            Assert.isTrue("timeout-22".equals(new HelloPrimaySecondaryCommand(22, false, 101).execute()));
        } finally {
            context.shutdown();
        }
    }

    private void hystrixObservableErrorCommand() {
        new HelloHystrixObservableErrorCommand().observe()
                .subscribe();
    }

    private void hystrixCollapseCommand() throws ExecutionException, InterruptedException {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Future<String> f1 = new HelloHystrixCollapserCommand(1).queue();
            Future<String> f2 = new HelloHystrixCollapserCommand(2).queue();
            Future<String> f3 = new HelloHystrixCollapserCommand(3).queue();
            Future<String> f4 = new HelloHystrixCollapserCommand(4).queue();

            Assert.isTrue("ValueForKey: 1".equals(f1.get()));
            Assert.isTrue("ValueForKey: 2".equals(f2.get()));
            Assert.isTrue("ValueForKey: 3".equals(f3.get()));
            Assert.isTrue("ValueForKey: 4".equals(f4.get()));

            // collapsed 되었기 때문에 1개
            Assert.isTrue(HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size() == 1);

            HystrixCommand<?> command = HystrixRequestLog.getCurrentRequest()
                    .getExecutedCommands()
                    .toArray(new HystrixCommand<?>[1])[0];

            Assert.isTrue("CollapseForKey".equals(command.getCommandKey().name()));
            Assert.isTrue(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED));
            Assert.isTrue(command.getExecutionEvents().contains(HystrixEventType.SUCCESS));

        } finally {
            context.shutdown();
        }
    }

    private void hystrixCacheCommand() {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            HelloHystrixCacheCommand command2a = new HelloHystrixCacheCommand(2);
            HelloHystrixCacheCommand command2b = new HelloHystrixCacheCommand(2);

            Assert.isTrue(command2a.execute());
            Assert.isTrue(!command2a.isResponseFromCache());
            Assert.isTrue(command2b.execute());
            Assert.isTrue(command2b.isResponseFromCache());

        } finally {
            context.shutdown();
        }

        context = HystrixRequestContext.initializeContext();
        try {
            HelloHystrixCacheCommand command3b = new HelloHystrixCacheCommand(2);
            Assert.isTrue(command3b.execute());
            Assert.isTrue(!command3b.isResponseFromCache());
        } finally {
            context.shutdown();
        }
    }

    private void hystrixObserverCommand() {
        new HelloHystrixObservableCommand("World").observe().subscribe(s -> {
            Assert.isTrue("Hello World!".equals(s));
        });

    }

    private void hystrixCommand() throws Exception {
        Assert.isTrue("Hello World!".equals(new HelloHystrixCommand("World").execute()));
        Assert.isTrue("Hello World!".equals(new HelloHystrixCommand("World").observe().toBlocking().single()));

        new HelloHystrixCommand("World").observe().subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String s) {
                Assert.isTrue("Hello World!".equals(s));
            }
        });

        new HelloHystrixCommand("World").observe().subscribe(s -> {
            Assert.isTrue("Hello World!".equals(s));
        });

        new HelloHystrixCommand("World").observe().subscribe(s -> {
            Assert.isTrue("Hello World!".equals(s));
        }, exception -> {
            exception.printStackTrace();
        });

        // async
        Future<String> fWorld = new HelloHystrixCommand("World").queue();
        Assert.isTrue("Hello World!".equals(fWorld.get()));

        // print Hot
        new HelloHystrixCommand("Hot", true).observe();
        // doesn't print Cold
        new HelloHystrixCommand("Cold", true).toObservable();

        Assert.isTrue("Woops Fail".equals(new HelloHystrixFallbackCommand("Fail").execute()));

    }
}
