package org.tyrannyofheaven.bukkit.util.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a command method. The method can have zero or more parameters.
 * The parameters may be primitive Java types (wrappers are OK) or one of
 * <ul>
 *   <li>org.bukkit.plugin.Plugin</li>
 *   <li>org.bukkit.Server</li>
 *   <li>org.bukkit.command.CommandSender</li>
 * </ul>
 * <p>Primitive type parameters or primitive wrapper parameters must be
 * annotated with <code>@Option</code>.
 * 
 * @see Option
 * @author zerothangel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * The name of the command along with any associated aliases.
     */
    public String[] value();

}
