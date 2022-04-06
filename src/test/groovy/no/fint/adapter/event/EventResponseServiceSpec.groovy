package no.fint.adapter.event

import no.fint.adapter.FintAdapterEndpoints
import no.fint.adapter.event.EventResponseService
import no.fint.event.model.Event
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class EventResponseServiceSpec extends Specification {
    private EventResponseService eventResponseService
    private FintAdapterEndpoints endpoints
    private RestTemplate restTemplate

    void setup() {
        endpoints = Mock()
        restTemplate = Mock(RestTemplate)
        eventResponseService = new EventResponseService(endpoints, restTemplate)
    }

    def "Post response"() {
        given:
        def event = new Event(orgId: 'rogfk.no')
        def component = 'test'

        when:
        eventResponseService.postResponse(component, event)

        then:
        1 * endpoints.getProviders() >> ['test': 'http://localhost']
        1 * endpoints.getResponse() >> '/response'
        1 * restTemplate.exchange('http://localhost/response', HttpMethod.POST, _ as HttpEntity, Void) >> ResponseEntity.ok().build()
    }
}