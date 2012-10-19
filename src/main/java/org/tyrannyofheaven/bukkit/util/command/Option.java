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
 * Annotates a primitive or primitive wrapper parameter. If the parameter is
 * optional (all flags are always optional, positional parameters are not
 * optional by default), you must use a wrapper type. Otherwise you will get
 * errors. Note however, that optional boolean parameters are OK (they will
 * default to false).
 * 
 * @author zerothangel
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
     * The name of the value, for display in the usage string. Note that
     * this is ignored for boolean/Boolean flags.
     */
    public String valueName() default "";
    
    /**
     * Whether or not this parameter is optional. Flags are always optional,
     * regardless of this setting.
     */
    public boolean optional() default false;

    /**
     * Whether or not this parameter is nullable. Only applies to non-optional,
     * non-flag parameters.
     */
    public boolean nullable() default false;

    /**
     * The name of the TypeCompleter to use. TypeCompleters should be registered
     * via {@link ToHCommandExecutor#registerTypeCompleter(String, TypeCompleter)}.
     */
    public String completer() default "";

}
