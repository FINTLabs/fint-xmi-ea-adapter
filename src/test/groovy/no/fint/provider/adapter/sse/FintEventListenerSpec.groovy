package no.fint.provider.adapter.sse

import no.fint.event.model.DefaultActions
import no.fint.event.model.Event

import org.glassfish.jersey.media.sse.InboundEvent
import spock.lang.Specification

class FintEventListenerSpec extends Specification {
    private FintEventListener fintEventListener
    private EventHandlerService eventHandlerService
    private InboundEvent inboundEvent

    void setup() {
        inboundEvent = Mock(InboundEvent)
        eventHandlerService = Mock(EventHandlerService)
        fintEventListener = new FintEventListener(eventHandlerService)
    }

    def "Handle incoming SSE event"() {
        given:
        def event = new Event(corrId: 'c978c986-8d50-496f-8afd-8d27bd68049b', action: DefaultActions.HEALTH.name(), orgId: 'rogfk.no', client: 'client')

        when:
        fintEventListener.onEvent(event)

        then:
        1 * eventHandlerService.handleEvent(event)
    }
}
