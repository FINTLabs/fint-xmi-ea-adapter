package no.fint.provider.adapter.event

import no.fint.event.model.Event
import no.fint.provider.adapter.FintAdapterProps
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class EventResponseServiceSpec extends Specification {
    private EventResponseService eventResponseService
    private FintAdapterProps props
    private RestTemplate restTemplate

    void setup() {
        props = Mock(FintAdapterProps)
        restTemplate = Mock(RestTemplate)
        eventResponseService = new EventResponseService(props: props, restTemplate: restTemplate)
    }

    def "Post response"() {
        given:
        def event = new Event(orgId: 'rogfk.no')

        when:
        eventResponseService.postResponse(event)

        then:
        1 * props.getResponseEndpoint() >> 'http://localhost'
        1 * restTemplate.exchange('http://localhost', HttpMethod.POST, _ as HttpEntity, Void) >> ResponseEntity.ok().build()
    }
}
