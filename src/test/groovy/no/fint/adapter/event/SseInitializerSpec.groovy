package no.fint.adapter.event

import no.fint.adapter.FintAdapterEndpoints
import no.fint.adapter.FintAdapterProps
import no.fint.adapter.sse.SseInitializer
import no.fint.eaxmi.service.EventHandlerService
import no.fint.oauth.TokenService
import no.fint.sse.FintSse
import spock.lang.Specification

class SseInitializerSpec extends Specification {
    private SseInitializer sseInitializer
    private FintAdapterProps props
    private FintAdapterEndpoints endpoints
    private FintSse fintSse
    private EventHandlerService eventHandlerService
    private TokenService tokenService

    void setup() {
        props = Mock(FintAdapterProps) {
            getOrganizations() >> ['rogfk.no', 'hfk.no', 'vaf.no']
        }
        endpoints = Mock(FintAdapterEndpoints) {
            getProviders() >> ['test':'http://localhost']
            getSse() >> '/sse/%s'
        }
        fintSse = Mock(FintSse)
        eventHandlerService = Mock(EventHandlerService)
        tokenService = Mock(TokenService)
    }

    def "Register and close SSE client for organizations"() {
        given:
        sseInitializer = new SseInitializer(props, endpoints, eventHandlerService, tokenService)

        when:
        sseInitializer.init()

        then:
        sseInitializer.sseClients.size() == 3
    }

    def "Check SSE connection"() {
        given:
        sseInitializer = new SseInitializer(props, endpoints, eventHandlerService, tokenService)
        sseInitializer.sseClients = [fintSse]

        when:
        sseInitializer.checkSseConnection()

        then:
        1 * fintSse.verifyConnection() >> true
    }

    def "Close SSE connection"() {
        given:
        sseInitializer = new SseInitializer(props, endpoints, eventHandlerService, tokenService)
        sseInitializer.sseClients = [fintSse]

        when:
        sseInitializer.cleanup()

        then:
        1 * fintSse.close()
    }
}