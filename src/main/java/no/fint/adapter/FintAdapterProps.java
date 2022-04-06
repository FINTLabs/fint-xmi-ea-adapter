package no.fint.adapter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FintAdapterProps {

    @Value("${fint.adapter.organizations}")
    private String[] organizations;

    @Value("${fint.adapter.sse-expiration:1200000}")
    private int expiration;

    @Value("${fint.adapter.reject-unknown-events:true}")
    private boolean rejectUnknownEvents;
}
