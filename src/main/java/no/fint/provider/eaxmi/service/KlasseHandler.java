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
public class KlasseHandler implements Handler {

    private final FintObjectService fintObjectService;

    public KlasseHandler(FintObjectService fintObjectService) {
        this.fintObjectService = fintObjectService;
    }

    @Override
    public void accept(Event<FintLinks> response) {
        fintObjectService.getClasses().forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(MetamodellActions.GET_ALL_KLASSE.name());
    }
}
