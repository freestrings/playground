package fs.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
		String reactorNettyIoWorkerCount = System.getProperty("reactor.netty.ioWorkerCount");
		System.out.println("#####################");
		System.out.println("reactorNettyIoWorkerCount: " + reactorNettyIoWorkerCount);
		System.out.println("#####################");

	}

}

