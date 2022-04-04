package no.fint.provider.eaxmi.service;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.metamodell.MetamodellActions;
import no.fint.model.resource.FintLinks;
import no.fint.provider.eaxmi.handler.Handler;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class RelasjonHandler implements Handler {

    private final FintObjectService fintObjectService;

    public RelasjonHandler(FintObjectService fintObjectService) {
        this.fintObjectService = fintObjectService;
    }

    @Override
    public void accept(Event<FintLinks> response) {
        fintObjectService.getRelations().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(MetamodellActions.GET_ALL_RELASJON.name());
    }
}
