package org.tyrannyofheaven.bukkit.util.command;

import java.util.Arrays;

final class OptionMetaData implements MethodParameter {

    private final String[] names;

    private final Class<?> type;
    
    private final boolean optional;
    
    public OptionMetaData(String[] names, Class<?> type, boolean optional) {
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("names must be given");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

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

    public boolean isArgument() {
        return isArgument(getName());
    }

    public static boolean isArgument(String name) {
        return !name.startsWith("-");
    }

}
