package org.tyrannyofheaven.bukkit.util.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a primitive or primitive wrapper parameter. If the parameter is
 * optional (all flags are always optional, positional parameters are not
 * optional by default), you must use a wrapper type. Otherwise you will get
 * errors. Note however, that optional boolean parameters are OK (they will
 * default to false).
 * 
 * @author asaddi
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {

    /**
     * The name of this mapping. Flags should start with a dash ('-'). Anything
     * not starting with '-' will be considered a positional parameter.
     */
    public String[] value();
    
    /**
     * Whether or not this parameter is optional. Flags are always optional,
     * regardless of this setting.
     */
    public boolean optional() default false;

}
