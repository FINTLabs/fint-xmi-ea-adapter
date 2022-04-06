package no.fint.eaxmi.handler;

import no.fint.event.model.Event;
import no.fint.model.resource.FintLinks;

import java.util.Collections;
import java.util.Set;

public class HealthHandler implements Handler {
    @Override
    public void accept(Event<FintLinks> fintLinksEvent) {
    }

    @Override
    public boolean health() {
        return true;
    }

    @Override
    public Set<String> actions() {
        return Collections.emptySet();
    }
}
