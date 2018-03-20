package no.fint.provider.adapter;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractSupportedActions {

    @Getter
    private List<String> actions = new ArrayList<>();

    public void add(Enum e) {
        actions.add(e.name());
    }

    public void addAll(Class<? extends Enum> e) {
        Enum[] enumConstants = e.getEnumConstants();
        actions.addAll(Arrays.stream(enumConstants).map(Enum::name).collect(Collectors.toList()));
    }

}
