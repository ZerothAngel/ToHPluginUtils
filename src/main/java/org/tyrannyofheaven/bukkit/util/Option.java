package org.tyrannyofheaven.bukkit.util;

public @interface Option {

    public String[] value();
    
    public boolean optional() default false;

}
