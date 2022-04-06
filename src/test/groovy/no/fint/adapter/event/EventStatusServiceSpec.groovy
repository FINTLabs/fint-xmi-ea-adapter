package no.fint.adapter.event

import no.fint.adapter.FintAdapterEndpoints
import no.fint.adapter.FintAdapterProps
import no.fint.adapter.event.EventStatusService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.eaxmi.SupportedActions
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class EventStatusServiceSpec extends Specification {
    private EventStatusService eventStatusService
    private FintAdapterEndpoints endpoints
    private FintAdapterProps props
    private SupportedActions supportedActions
    private RestTemplate restTemplate

    void setup() {
        restTemplate = Mock(RestTemplate)
        endpoints = Mock()
        props = Mock()
        supportedActions = new SupportedActions()
        eventStatusService = new EventStatusService(endpoints, restTemplate, supportedActions, props)
    }

    def "Verify event and POST event status"() {
        given:
        def event = new Event(orgId: 'rogfk.no', action: DefaultActions.HEALTH.name())
        def component = 'test'

        when:
        def verifiedEvent = eventStatusService.verifyEvent(component, event)

        then:
        1 * endpoints.getProviders() >> ['test': 'http://localhost']
        1 * endpoints.getStatus() >> '/status'
        1 * restTemplate.exchange('http://localhost/status', _ as HttpMethod, _ as HttpEntity, _ as Class) >> ResponseEntity.ok().build()
        verifiedEvent
    }
}
