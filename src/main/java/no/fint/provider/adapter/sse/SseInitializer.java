package no.fint.provider.adapter.sse;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.oauth.TokenService;
import no.fint.provider.adapter.FintAdapterProps;
import no.fint.provider.eaxmi.service.EventHandlerService;
import no.fint.sse.FintSse;
import no.fint.sse.FintSseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the client connections to the provider SSE endpoint
 */
@Slf4j
@Component
public class SseInitializer {

    @Getter
    private List<FintSse> sseClients = new ArrayList<>();

    @Autowired
    private FintAdapterProps props;

    @Autowired
    private EventHandlerService eventHandlerService;

    @Autowired(required = false)
    private TokenService tokenService;

    @PostConstruct
    public void init() {
        FintSseConfig config = FintSseConfig.withOrgIds(props.getOrganizations());
        Arrays.asList(props.getOrganizations()).forEach(orgId -> {
            FintSse fintSse = new FintSse(props.getSseEndpoint(), tokenService, config);
            FintEventListener fintEventListener = new FintEventListener(eventHandlerService);
            fintSse.connect(fintEventListener, ImmutableMap.of(HeaderConstants.ORG_ID, orgId));
            sseClients.add(fintSse);
        });
    }

    @Scheduled(initialDelay = 20000L, fixedDelay = 5000L)
    public void checkSseConnection() {
        try {
            long oldest = sseClients.stream().mapToLong(FintSse::getAge).max().orElse(0);
            if (oldest > 0 && oldest > props.getExpiration()) {
                log.warn("Stale connection detected (oldest {} ms ago), restarting!!", oldest);
                cleanup();
                init();
            } else {
                for (FintSse sseClient : sseClients) {
                    if (!sseClient.verifyConnection()) {
                        log.info("Reconnecting SSE client");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error during SSE connection check!", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        List<FintSse> oldClients = sseClients;
        sseClients = new ArrayList<>();
        for (FintSse sseClient : oldClients) {
            sseClient.close();
        }
    }
}
