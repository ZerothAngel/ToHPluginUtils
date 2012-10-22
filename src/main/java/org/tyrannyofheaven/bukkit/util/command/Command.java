/*
 * Copyright 2011 ZerothAngel <zerothangel@tyrannyofheaven.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     * Command description to be displayed when showing help.
     */
    public String description() default "";

    /**
     * Description of the varargs (String[]) parameter when showing help.
     */
    public String varargs() default "";

    /**
     * TypeCompleter to use for varargs. Also see {@link ToHCommandExecutor#registerTypeCompleter(String, TypeCompleter)}.
     */
    public String completer() default "";

}
