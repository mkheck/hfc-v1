package com.thehecklers.thing2service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class Thing2ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Thing2ServiceApplication.class, args);
    }

}

@Controller
@RequestMapping("/thingtwothings")
class ThingController {
    private final List<RSocketRequester> rsClients = new ArrayList<>();
    private Random rnd = new Random();
    private List<String> names = List.of("Alpha", "Bravo", "Charlie", "Delta", "Echo");

    @ResponseBody
    @GetMapping("/thing2")
    Mono<Thing2> getThing() {
        return Mono.just(new Thing2(UUID.randomUUID().toString(),
                "Thing 2 from thing2-service"));
    }

    @ResponseBody
    @GetMapping("/afewthing2s")
    Flux<Thing2> getThings() {
        return Flux.fromStream(
                names.stream()
                        .map(name -> new Thing2(UUID.randomUUID().toString(), name)));

    }

    @ResponseBody
    @GetMapping(value = "/thing2stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Thing2> getThingStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(l -> new Thing2(UUID.randomUUID().toString(),
                        names.get(rnd.nextInt(names.size()))))
                .log();
    }

    @ConnectMapping("rsclient")
    void connectClient(RSocketRequester requester, @Payload String clientId) {
        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    System.out.println("Client connected: " + clientId);
                    rsClients.add(requester);
                })
                .doOnError(err -> System.out.println("Channel to client closed: " + clientId))
                .doFinally(consumer -> {
                    rsClients.remove(requester);
                    System.out.println("Client disconnnected: " + clientId);
                })
                .subscribe();

        requester.route("clientcall")
                .data("Greetings Thing1")
                .retrieveFlux(Thing1.class)
//                .doOnNext(t1 -> System.out.println("  >> Received T1: " + t1))
                .subscribe(System.out::println);
    }

    @MessageMapping("thing2")
    Mono<Thing2> getThingRS() { // No param? 1:1
        return Mono.just(new Thing2(UUID.randomUUID().toString(),
                "Thing 2 from thing2-service"));
    }

    @MessageMapping("afewthing2s")
    Flux<Thing2> getThingsRS() { // No param? 1:N
        return Flux.fromStream(
                names.stream()
                        .map(name -> new Thing2(UUID.randomUUID().toString(), name)));

    }

    @MessageMapping(value = "thing2stream")
    Flux<Thing2> getThingStreamRS() { // No param? 1:N forever
        return Flux.interval(Duration.ofSeconds(1))
                .map(l -> new Thing2(UUID.randomUUID().toString(),
                        names.get(rnd.nextInt(names.size()))))
                .log();
    }

    @MessageMapping("pingpong")
    Flux<Thing2> bidirectionalChannel(Flux<Thing1> things) {
        return things
                .doOnNext(thing1 -> System.out.println("  >>> Received: " + thing1))
                .switchMap(thing1 -> Flux.just(new Thing2(UUID.randomUUID().toString(),
                        names.get(rnd.nextInt(names.size())))))
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