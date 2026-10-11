package com.mycompany.visitcounter.controller;

import com.mycompany.visitcounter.service.CounterService;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class CounterController {

    private static final String TEXT = "text/plain;charset=UTF-8";

    private final CounterService counterService;
    private final String containerId;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
        this.containerId = resolveContainerId();
    }

    @GetMapping(value = "/", produces = TEXT)
    public String index() {
        long hits = counterService.incrementHits();
        return "Bonjour ! Cette page a été vue " + hits + " fois. Je suis le conteneur " + containerId;
    }

    @GetMapping(value = "/health", produces = TEXT)
    public String health() {
        return "OK";
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<String> redisUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Content-Type", TEXT)
                .body("Erreur : impossible de joindre Redis (db-service).");
    }

    // Dans Docker, le hostname d'un conteneur = son ID court (12 caractères)
    static String resolveContainerId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return System.getenv().getOrDefault("HOSTNAME", "inconnu");
        }
    }
}
