package no.fint.adapter.sse;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.provider.eaxmi.service.EventHandlerService;
import no.fint.sse.AbstractEventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

/**
 * Event listener for the for the SSE client. When an inbound event is received the {@link #onEvent(InboundEvent)} method
 * calls {@link EventHandlerService} service.
 */
@Slf4j
public class FintEventListener extends AbstractEventListener {

    private final EventHandlerService eventHandler;
    private final String component;

    public FintEventListener(String component, EventHandlerService eventHandler) {
        this.eventHandler = eventHandler;
        this.component = component;
    }

    @Override
    public void onEvent(Event event) {
        log.info("{}: Processing event {} for {} - {}", component, event.getAction(), event.getOrgId(), event.getCorrId());
        eventHandler.handleEvent(component, event);
    }
}
