package fs.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableWebFlux
public class ReactorApplication {

    static Logger log = LoggerFactory.getLogger(ReactorApplication.class);

    public static void main(String[] args) {
//        SpringApplication.run(DemoApplication.class, args);

        MonoApis monoApis = new MonoApis();
//        monoApis.monoAnd();
//        monoApis.block();
//        monoApis.cache();
//        monoApis.cancel();
//        monoApis.cast();
//        monoApis.checkpoint();
//        monoApis.concatWith();
//        monoApis.defer();
//        monoApis.materalize();
//        monoApis.demateralize();
//        monoApis.doAfterTerminate();
//        monoApis.doFinally();
//        monoApis.doFirst();
//        monoApis.doOnDiscard();
//        monoApis.elapsed();
        monoApis.expand();
    }

    @RestController
    class Ctrls {

        @GetMapping("/")
        public <T> Mono<ResponseEntity<Resp<T>>> mono(
                @RequestParam(value = "value") T value,
                @RequestParam(value = "delay") int delay) {

            log.info("http call mono: {}, {}", value, delay);

            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }
            return Mono.just(ResponseEntity.ok().body(new Resp<>(value)));
        }
    }
}
