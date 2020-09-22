package com.thehecklers.thing2service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@SpringBootApplication
public class Thing2ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Thing2ServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("/thing2")
class ThingController {
    @GetMapping
    Thing2 getThing() {
        return new Thing2(UUID.randomUUID().toString(),
                "Thing 2 from thing2-service");
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