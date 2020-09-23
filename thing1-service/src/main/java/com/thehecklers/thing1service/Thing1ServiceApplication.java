package com.thehecklers.thing1service;

import io.rsocket.SocketAcceptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.UUID;

@SpringBootApplication
public class Thing1ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Thing1ServiceApplication.class, args);
    }

}

@Component
class RSComponent {
    private final RSocketRequester requester;

    public RSComponent(RSocketRequester.Builder builder) {
        this.requester = builder.connectTcp("localhost", 8091).block();
    }

    //    @PostConstruct
    void oneToOne() {
        requester.route("thing2")
                .data(new Thing1(UUID.randomUUID().toString(), "RSocket Thing1"))
                .retrieveMono(Thing2.class)
                .subscribe(t2 -> System.out.println("   >>> T2: " + t2));
    }

    //	@PostConstruct
    void oneToManyFinite() {
        requester.route("afewthing2s")
                .data(new Thing1(UUID.randomUUID().toString(), "RSocket Thing1"))
                .retrieveFlux(Thing2.class)
                .subscribe(t2 -> System.out.println("   >>> T2: " + t2));
    }

    //    @PostConstruct
    void oneToManyInfinite() {
        requester.route("thing2stream")
                .data(new Thing1(UUID.randomUUID().toString(), "RSocket Thing1"))
                .retrieveFlux(Thing2.class)
                .take(10)
                .onBackpressureDrop()
                .subscribe(t2 -> System.out.println("   >>> T2: " + t2));
    }

    @PostConstruct
    void manyToMany() {
        requester.route("pingpong")
                .data(Flux.interval(Duration.ofSeconds(1))
                        .map(l -> new Thing1(UUID.randomUUID().toString(), "RSocket Thing1")))
                .retrieveFlux(Thing2.class)
                .subscribe(t2 -> System.out.println("   >>> T2: " + t2));
    }
}

//@Controller
//class RSController {
class RSClientHandler {
    @MessageMapping("clientcall")
    public Flux<Thing1> helloAreYouThere() { //(String status) {
        System.out.println(" >> Hello? Is it me you're looking for?");
        return Flux.interval(Duration.ofSeconds(1))
                .map(l -> new Thing1(UUID.randomUUID().toString(),
                        "Thing1 coming to you from thing1-service!"));
    }
}

class RSClient {
    private RSocketRequester rsocketRequester;
    private RSocketRequester.Builder rsocketRequesterBuilder;
    private RSocketStrategies rsocketStrategies;
    private String clientId = UUID.randomUUID().toString();

    public RSClient(RSocketRequester.Builder builder,
                    RSocketStrategies strategies) {
        this.rsocketRequesterBuilder = builder;
        this.rsocketStrategies = strategies;

        SocketAcceptor acceptor = RSocketMessageHandler.responder(strategies, new RSClientHandler());

        this.rsocketRequester = rsocketRequesterBuilder
                .setupRoute("rsclient")
                .setupData(clientId)
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(acceptor))
                .connectTcp("localhost", 8081)
                .block();

        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(error -> System.out.println("Connection CLOSED"))
                .doFinally(consumer -> System.out.println("Client DISCONNECTED"))
                .subscribe();
    }

    @PreDestroy
    public void cleanUp() {
        this.rsocketRequester.rsocket().dispose();
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