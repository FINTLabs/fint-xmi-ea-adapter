package no.fint.adapter;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractSupportedActions {

    @Getter
    private List<String> actions = new ArrayList<>();

    public void add(Enum e) {
        actions.add(e.name());
    }

    public void add(String name) {
        actions.add(name);
    }

    public void addAll(Class<? extends Enum> e) {
        Stream.of(e.getEnumConstants()).map(Enum::name).forEach(actions::add);
    }

    public boolean supports(String action) {
        return actions.contains(action);
    }
}
