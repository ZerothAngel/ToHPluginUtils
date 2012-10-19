/*
 * Copyright 2012 ZerothAngel <zerothangel@tyrannyofheaven.org>
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

import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Responsible for generating possible values for tab-completion.
 * 
 * @author zerothangel
 */
public interface TypeCompleter {

    /**
     * Generate possible values for the given parameter type and argument.
     * 
     * @param clazz the parameter type
     * @param arg the argument. May be null.
     * @param sender TODO
     * @param partial start of string to match for completion. May be empty, never null.
     * @return
     */
    public List<String> complete(Class<?> clazz, String arg, CommandSender sender, String partial);

}
