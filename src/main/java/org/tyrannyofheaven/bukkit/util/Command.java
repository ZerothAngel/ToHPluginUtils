package org.tyrannyofheaven.bukkit.util;

public @interface Command {

    public String value();

    public String[] aliases() default { };

    public int minArgs = 0;
    
    public int maxArgs = 0;

}
