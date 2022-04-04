package no.fint.adapter.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.adapter.sse.SseInitializer;
import no.fint.sse.FintSse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    final
    SseInitializer sseInitializer;

    public AdminController(SseInitializer sseInitializer) {
        this.sseInitializer = sseInitializer;
    }

    @GetMapping("/sse")
    public List<FintSse> getSseConnections() {
        return sseInitializer.getSseClients();
    }

    @DeleteMapping("/sse")
    public void destroySseConnections() {
        sseInitializer.cleanup();
    }

    @PostMapping("/sse")
    public void initSseConnections() {
        sseInitializer.init();
    }
}
