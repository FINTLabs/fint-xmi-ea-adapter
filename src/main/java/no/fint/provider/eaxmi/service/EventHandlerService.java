package no.fint.provider.eaxmi.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.model.metamodell.MetamodellActions;
import no.fint.model.resource.FintLinks;
import no.fint.provider.adapter.event.EventResponseService;
import no.fint.provider.adapter.event.EventStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Slf4j
@Service
public class EventHandlerService {

    @Autowired
    private EventResponseService eventResponseService;

    @Autowired
    private EventStatusService eventStatusService;

    @Autowired
    private FintObjectService fintObjectService;

    public void handleEvent(Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(event);
        } else {
            if (eventStatusService.verifyEvent(event).getStatus() == Status.ADAPTER_ACCEPTED) {
                MetamodellActions action = MetamodellActions.valueOf(event.getAction());
                Event<FintLinks> responseEvent = new Event<>(event);

                switch (action) {
                    case GET_ALL_KONTEKST:
                        fintObjectService.getContexts().forEach(responseEvent::addData);
                        break;

                    case GET_ALL_KLASSE:
                        fintObjectService.getClasses().forEach(responseEvent::addData);
                        break;

                    case GET_ALL_RELASJON:
                        fintObjectService.getRelations().forEach(responseEvent::addData);
                        break;

                }

                log.info("Response to {}, {} items", action, responseEvent.getData().size());
                responseEvent.setStatus(Status.ADAPTER_RESPONSE);
                eventResponseService.postResponse(responseEvent);
            }
        }
    }


    public void postHealthCheckResponse(Event event) {
        Event<Health> healthCheckEvent = new Event<>(event);
        healthCheckEvent.setStatus(Status.TEMP_UPSTREAM_QUEUE);

        if (healthCheck()) {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_HEALTHY.name()));
        } else {
            healthCheckEvent.addData(new Health("adapter", HealthStatus.APPLICATION_UNHEALTHY));
            healthCheckEvent.setMessage("The adapter is unable to communicate with the application.");
        }

        eventResponseService.postResponse(healthCheckEvent);
    }


    private boolean healthCheck() {
        /*
         * Check application connectivity etc.
         */
        return true;
    }


    @PostConstruct
    void init() {
        fintObjectService.update();
    }

}

