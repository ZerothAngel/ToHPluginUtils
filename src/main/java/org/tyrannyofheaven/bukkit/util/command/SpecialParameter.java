package org.tyrannyofheaven.bukkit.util.command;

/**
 * One of the special parameters types that automatically receive a value
 * from the framework.
 * 
 * @author zerothangel
 */
final class SpecialParameter implements MethodParameter {

    private final Type type;
    
    /**
     * Create a SpecialParameter instance.
     * 
     * @param type the special type of parameter
     */
    public SpecialParameter(Type type) {
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

        this.type = type;
    }

    /**
     * Retrieve the SpecialParameter type.
     * 
     * @return the type
     */
    public Type getType() {
        return type;
    }

    enum Type {
        /**
         * org.bukkit.Server
         */
        SERVER,
        
        /**
         * org.bukkit.plugin.Plugin or subclasses
         */
        PLUGIN,
        
        /**
         * org.bukkit.plugin.CommandSender
         */
        COMMAND_SENDER,
        
        /**
         * String[] parameter annotated with @Rest
         */
        REST;
    }

}
