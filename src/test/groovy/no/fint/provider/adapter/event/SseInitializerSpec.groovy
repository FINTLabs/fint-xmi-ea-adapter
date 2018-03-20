package no.fint.provider.adapter.event

import no.fint.provider.adapter.FintAdapterProps
import no.fint.provider.adapter.sse.SseInitializer
import no.fint.sse.FintSse
import spock.lang.Specification

class SseInitializerSpec extends Specification {
    private SseInitializer sseInitializer
    private FintAdapterProps props
    private FintSse fintSse

    void setup() {
        props = Mock(FintAdapterProps) {
            getOrganizations() >> ['rogfk.no', 'hfk.no', 'vaf.no']
            getSseEndpoint() >> 'http://localhost'
        }
        fintSse = Mock(FintSse)
    }

    def "Register and close SSE client for organizations"() {
        given:
        sseInitializer = new SseInitializer(props: props)

        when:
        sseInitializer.init()

        then:
        sseInitializer.sseClients.size() == 3
    }

    def "Check SSE connection"() {
        given:
        sseInitializer = new SseInitializer(props: props, sseClients: [fintSse])

        when:
        sseInitializer.checkSseConnection()

        then:
        1 * fintSse.verifyConnection() >> true
    }

    def "Close SSE connection"() {
        given:
        sseInitializer = new SseInitializer(props: props, sseClients: [fintSse])

        when:
        sseInitializer.cleanup()

        then:
        1 * fintSse.close()
    }
}
