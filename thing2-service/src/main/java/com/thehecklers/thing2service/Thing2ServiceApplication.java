package com.thehecklers.thing2service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
public class Thing2ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Thing2ServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("/thingtwothings")
class ThingController {
    private Random rnd = new Random();
    private List<String> names = List.of("Alpha", "Bravo", "Charlie", "Delta", "Echo");

    @GetMapping("/thing2")
    Mono<Thing2> getThing() {
        return Mono.just(new Thing2(UUID.randomUUID().toString(),
                "Thing 2 from thing2-service"));
    }

    @GetMapping("/afewthing2s")
    Flux<Thing2> getThings() {
        return Flux.fromStream(
                names.stream()
                        .map(name -> new Thing2(UUID.randomUUID().toString(), name)));

    }

    @GetMapping(value = "/thing2stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Thing2> getThingStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(l -> new Thing2(UUID.randomUUID().toString(),
                        names.get(rnd.nextInt(names.size()))))
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