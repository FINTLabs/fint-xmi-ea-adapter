package no.fint.provider.eaxmi.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.model.metamodell.MetamodellActions;
import no.fint.model.relation.FintResource;
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

    @Autowired
    private XmiParserService xmiParserService;


    public void handleEvent(Event event) {
        if (event.isHealthCheck()) {
            postHealthCheckResponse(event);
        } else {
            if (event != null && eventStatusService.verifyEvent(event).getStatus() == Status.ADAPTER_ACCEPTED) {
                MetamodellActions action = MetamodellActions.valueOf(event.getAction());
                Event<FintResource> responseEvent = new Event<>(event);

                switch (action) {
                    case GET_ALL_PAKKE:
                        responseEvent.setData(fintObjectService.getPackages());
                        break;

                    case GET_ALL_KLASSE:
                        responseEvent.setData(fintObjectService.getClasses());
                        break;

                }

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


        xmiParserService.getXmiDocument();

    }

}

