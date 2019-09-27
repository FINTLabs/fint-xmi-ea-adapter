package no.fint;

import no.fint.oauth.OAuthTokenProps;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {
    @ConditionalOnProperty(name = OAuthTokenProps.ENABLE_OAUTH,
            matchIfMissing = true, havingValue = "false")
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
