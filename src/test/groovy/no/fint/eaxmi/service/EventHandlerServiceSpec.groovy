package no.fint.eaxmi.service

import no.fint.adapter.event.EventResponseService
import no.fint.adapter.event.EventStatusService
import no.fint.eaxmi.SupportedActions
import no.fint.eaxmi.handler.Handler
import no.fint.eaxmi.service.EventHandlerService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import spock.lang.Specification

class EventHandlerServiceSpec extends Specification {
    private EventHandlerService eventHandlerService
    private EventStatusService eventStatusService
    private EventResponseService eventResponseService

    void setup() {
        eventStatusService = Mock(EventStatusService)
        eventResponseService = Mock(EventResponseService)
        eventHandlerService = new EventHandlerService(eventResponseService, eventStatusService, Mock(SupportedActions), [])
    }

    def "Post response on health check"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH, 'test')

        when:
        eventHandlerService.handleEvent("test", event)

        then:
        1 * eventResponseService.postResponse(_ as String, _ as Event)
    }
}
