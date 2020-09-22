package com.thehecklers.thing1service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@SpringBootApplication
public class Thing1ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Thing1ServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("/things")
class ThingsController {
	private final WebClient client = WebClient.create("http://localhost:8090/thingtwothings");

	@GetMapping("/thing1")
	Thing1 getThing1() {
		return new Thing1(UUID.randomUUID().toString(),
				"Thing 1 from thing1-service");
	}

	@GetMapping(value = "/afewthing2s", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Thing2> getThing2s() {
		return client.get()
				.uri("/afewthing2s")
				.retrieve()
				.bodyToFlux(Thing2.class)
				.delayElements(Duration.ofSeconds(1))
				.log();
	}

	@GetMapping(value = "/thing2stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Thing2> getThing2StreamFromThing2Service() {
		return client.get()
				.uri("/thing2stream")
				.retrieve()
				.bodyToFlux(Thing2.class)
				.log();
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Thing1 {
    private String id, description;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Thing2 {
    private String id, description;
}