package no.fint.provider.eaxmi;

import no.fint.model.metamodell.MetamodellActions;
import no.fint.provider.adapter.AbstractSupportedActions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SupportedActions extends AbstractSupportedActions {

    @PostConstruct
    public void addSupportedActions() {
        addAll(MetamodellActions.class);
    }

}
