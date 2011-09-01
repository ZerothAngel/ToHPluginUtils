package org.tyrannyofheaven.bukkit.util.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a command method, specifying any required permissions to execute
 * (or maybe even see) the command.
 * 
 * @author zerothangel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Require {

    /**
     * One or more permission names.
     */
    public String[] value();

    /**
     * Whether all permissions are required.
     */
    public boolean all() default false;
    
}
