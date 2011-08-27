package org.tyrannyofheaven.bukkit.util.command;

import java.util.Arrays;

public class OptionMetaData implements MethodParameter {

    private final String[] names;

    private final Class<?> type;
    
    private final boolean optional;
    
    public OptionMetaData(String[] names, Class<?> type, boolean optional) {
        this.names = Arrays.copyOf(names, names.length);
        this.type = type;
        this.optional = optional;
    }

    public String getName() {
        return names[0];
    }

    public String[] getNames() {
        return names;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

}
