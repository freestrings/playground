package fs.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class ReactorApplication {

    static Logger log = LoggerFactory.getLogger(ReactorApplication.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ReactorApplication.class, args);

//        MonoApis monoApis = new MonoApis();
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
//        monoApis.expand();
//        monoApis.first();
//        monoApis.flatMap();
//        monoApis.from();
//        monoApis.fromCompletionStage();
//        monoApis.handle();
//        monoApis.mapAndFlatMap();
//        monoApis.mergeWith();
//        monoApis.onErrorContinue();
//        monoApis.onErrorResume();
//        monoApis.onErrorStop();
//        monoApis.publish();
//        monoApis.retry();
//        monoApis.sequenceEqual();
//        monoApis.tag();
//        monoApis.then();
//        monoApis.zip();

//        FluxApis fluxApis = new FluxApis();
//        fluxApis.all();
//        fluxApis.buffer();
//        fluxApis.collect();
//        fluxApis.combineLatest();
    }
}
