package no.fint.adapter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fint.adapter.endpoints")
public class FintAdapterEndpoints {
    private String sse;
    private String status;
    private String response;
    private Map<String, String> providers;
}