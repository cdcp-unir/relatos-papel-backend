package relatos_de_papel_eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class BackEndEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackEndEurekaApplication.class, args);
	}

}
