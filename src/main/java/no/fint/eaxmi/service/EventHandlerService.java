package no.fint.eaxmi.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.adapter.event.EventResponseService;
import no.fint.adapter.event.EventStatusService;
import no.fint.eaxmi.handler.Handler;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.model.resource.FintLinks;
import no.fint.eaxmi.SupportedActions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Slf4j
@Service
public class EventHandlerService {

    private final EventResponseService eventResponseService;

    private final EventStatusService eventStatusService;

    private final SupportedActions supportedActions;

    private final Collection<Handler> handlers;

    @Getter
    private Map<String, Handler> actionsHandlerMap;

    private Executor executor;

    public EventHandlerService(EventResponseService eventResponseService, EventStatusService eventStatusService, SupportedActions supportedActions, Collection<Handler> handlers) {
        this.eventResponseService = eventResponseService;
        this.eventStatusService = eventStatusService;
        this.supportedActions = supportedActions;
        this.handlers = handlers;
    }

    public void handleEvent(String component, Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(component, event);
        } else {
            if (eventStatusService.verifyEvent(component, event)) {
                executor.execute(() ->
                        handleResponse(component, event.getAction(), new Event<>(event)));
            }
        }
    }

    private void handleResponse(String component, String action, Event<FintLinks> response) {
        try {
            actionsHandlerMap.getOrDefault(action, e -> {
                log.warn("No handler found for {}", e.getAction());
                e.setStatus(Status.ADAPTER_REJECTED);
                e.setResponseStatus(ResponseStatus.REJECTED);
                e.setMessage("Unsupported action");
            }).accept(response);
        } catch (IllegalArgumentException e) {
            log.warn("Illegal arguments in event {}: {}", response, e.getMessage());
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Error handling event {}", response, e);
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(e.getMessage());
        } finally {
            if (response.getData() != null) {
                log.info("{}: Response for {}: {}, {} items", component, response.getAction(), response.getResponseStatus(), response.getData().size());
                log.trace("Event data: {}", response.getData());
            } else {
                log.info("{}: Response for {}: {}", component, response.getAction(), response.getResponseStatus());
            }
            eventResponseService.postResponse(component, response);
        }
    }

    public void postHealthCheckResponse(String component, Event event) {
        Event<Health> healthCheckEvent = new Event<>(event);
        healthCheckEvent.setStatus(Status.TEMP_UPSTREAM_QUEUE);

        try {
            if (healthCheck()) {
                healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_HEALTHY));
            } else {
                healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
                healthCheckEvent.setMessage("The adapter is unable to communicate with the application.");
            }
        } catch (Exception e) {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
            healthCheckEvent.setMessage(e.getMessage());
        }

        eventResponseService.postResponse(component, healthCheckEvent);
    }


    private boolean healthCheck() {
        return handlers.stream().allMatch(Handler::health);
    }

    @PostConstruct
    void init() {
        executor = Executors.newSingleThreadExecutor();
        actionsHandlerMap = new HashMap<>();
        handlers.forEach(h -> h.actions().forEach(a -> {
            actionsHandlerMap.put(a, h);
            supportedActions.add(a);
        }));
        log.info("Registered {} handlers, supporting actions: {}", handlers.size(), supportedActions.getActions());
    }

}
