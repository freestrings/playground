package fs.playground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.time.Duration;

public class Tests {

    @Test
    public void testStep() {
        StepVerifier.create(Flux.just("a", "b").concatWith(Mono.error(new IllegalArgumentException("err"))))
                .expectNext("a")
                .expectNext("b")
                .expectError(IllegalArgumentException.class)
                .verify()
        ;
    }

    @Test
    public void testTime() {
        StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofDays(1)))
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .expectComplete()
                .verify()
        ;

        StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofDays(1)))
                .expectSubscription()
                .expectNoEvent(Duration.ofDays(1))
                .expectNext(0L)
                .verifyComplete()
        ;
    }

    @Test
    public void testDoOn() {
        PublisherProbe<Object> probe = PublisherProbe.empty();

        StepVerifier.create(Mono.empty().switchIfEmpty(probe.mono()))
                .verifyComplete();

        probe.assertWasSubscribed();
        probe.assertWasRequested();
        probe.assertWasNotCancelled();
    }
}
