package org.tyrannyofheaven.bukkit.util.command;

final class SpecialParameter implements MethodParameter {

    private final Type type;
    
    public SpecialParameter(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    enum Type {
        SERVER,
        PLUGIN,
        COMMAND_SENDER,
        REST;
    }

}
