package fs.mvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MvcApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(MvcApplication.class, args);
		String tomcatMaxThreads = context.getEnvironment().getProperty("server.tomcat.max-threads");
		System.out.println("#####################");
		System.out.println("tomcatMaxThreads: " + tomcatMaxThreads);
		System.out.println("#####################");
	}

}
