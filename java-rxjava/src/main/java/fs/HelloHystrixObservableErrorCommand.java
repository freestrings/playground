package fs;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class HelloHystrixObservableErrorCommand extends HystrixObservableCommand {

    private Integer lastSeen = 0;

    protected HelloHystrixObservableErrorCommand() {
        super(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"));
    }

    @Override
    protected Observable construct() {

        // concat은 두개이상의 Observable을 순차적으로 합친다.
        // 1--1-----1
        // -2-2------
        // 1--1-----1-2-2 <= 순차적으로 합친다.
        return Observable.just(1, 2, 3)
                .concatWith(Observable.<Integer> error(new RuntimeException("forced error")))
                .doOnNext(t1 -> {
                    System.out.format("doOnNext %d%n", t1);
                    lastSeen = t1;
                })
                .subscribeOn(Schedulers.computation());
    }

    @Override
    protected Observable<Integer> resumeWithFallback() {
        System.out.format("LastSeen: %d%n", lastSeen);
        if (lastSeen < 4) {
            return Observable.range(lastSeen + 1, 4 - lastSeen);
        } else {
            return Observable.empty();
        }
    }
}
