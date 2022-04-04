package no.fint.provider.eaxmi.handler;

import no.fint.event.model.Event;
import no.fint.model.resource.FintLinks;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public interface Handler extends Consumer<Event<FintLinks>> {

    default Set<String> actions() {
        return Collections.emptySet();
    }

    default boolean health() {
        return true;
    }
}
