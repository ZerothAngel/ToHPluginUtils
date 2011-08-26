package org.tyrannyofheaven.bukkit.util;

public @interface Require {

    public String[] value();
    
    public boolean all() default false;

}
