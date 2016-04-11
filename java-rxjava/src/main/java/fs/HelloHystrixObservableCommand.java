package fs;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;
import rx.Subscriber;

public class HelloHystrixObservableCommand extends HystrixObservableCommand {

    private final String name;

    public HelloHystrixObservableCommand(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"));
        this.name = name;
    }

    @Override
    protected Observable<String> construct() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> observer) {
                try {
                    if (observer.isUnsubscribed()) {
                        return;
                    }

                    observer.onNext("Hello " + name + "!");
                    observer.onCompleted();
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        });
    }
}
