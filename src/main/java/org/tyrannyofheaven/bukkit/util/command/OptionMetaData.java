package org.tyrannyofheaven.bukkit.util.command;

import java.util.Arrays;

/**
 * Metadata for mapped parameters.
 * 
 * @author zerothangel
 */
final class OptionMetaData implements MethodParameter {

    private final String[] names;

    private final Class<?> type;
    
    private final boolean optional;
    
    /**
     * Create an OptionMetaData.
     * 
     * @param names the name of the mapping along with any aliases
     * @param type the parameter type
     * @param optional true if optional
     */
    public OptionMetaData(String[] names, Class<?> type, boolean optional) {
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("names must be given");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

        this.names = Arrays.copyOf(names, names.length);
        this.type = type;
        this.optional = optional;
    }

    /**
     * Returns the primary name of the option.
     * 
     * @return the primary name
     */
    public String getName() {
        return names[0];
    }

    /**
     * Returns all names of the option.
     * 
     * @return all names of the option
     */
    public String[] getNames() {
        return names;
    }

    /**
     * Returns the type of the option.
     * 
     * @return the type of the option
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns whether or not the option is optional.
     * 
     * @return true if optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Returns whether or not the option is a flag or a positional parameter.
     * 
     * @return true if a positional parameter, false if a flag
     */
    public boolean isArgument() {
        return isArgument(getName());
    }

    /**
     * Returns whether or not an option with the given name is a flag or a
     * positional parameter.
     * 
     * @return true if a positional parameter, false if a flag
     */
    public static boolean isArgument(String name) {
        return !name.startsWith("-");
    }

}
