package no.fint.provider.adapter.event;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.DefaultActions;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.event.model.Status;
import no.fint.provider.adapter.FintAdapterProps;
import no.fint.provider.eaxmi.SupportedActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Handles statuses back to the provider status endpoint.
 */
@Slf4j
@Service
public class EventStatusService {

    @Autowired
    private FintAdapterProps props;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SupportedActions supportedActions;

    /**
     * Verifies if we can handle the event and set the status accordingly.
     *
     * @param event
     * @return The inbound event.
     */
    public Event verifyEvent(Event event) {
        if (supportedActions.getActions().contains(event.getAction()) || DefaultActions.getDefaultActions().contains(event.getAction())) {
            event.setStatus(Status.ADAPTER_ACCEPTED);
        } else {
            event.setStatus(Status.ADAPTER_REJECTED);
        }

        postStatus(event);
        return event;
    }

    /**
     * Method for posting back the status to the provider.
     *
     * @param event
     */
    public void postStatus(Event event) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.put(HeaderConstants.ORG_ID, Lists.newArrayList(event.getOrgId()));
            ResponseEntity<Void> response = restTemplate.exchange(props.getStatusEndpoint(), HttpMethod.POST, new HttpEntity<>(event, headers), Void.class);
            log.info("Provider POST status response: {}", response.getStatusCode());
        } catch (RestClientException e) {
            log.error("Unable to POST status for {}: {}", event, e.getMessage());
        }
    }
}
