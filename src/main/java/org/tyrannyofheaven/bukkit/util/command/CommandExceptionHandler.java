/*
 * Copyright 2013 ZerothAngel <zerothangel@tyrannyofheaven.org>
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

import org.bukkit.command.CommandSender;

/**
 * Allows customized handling of exceptions thrown by command handlers.
 * 
 * @author zerothangel
 */
public interface CommandExceptionHandler {

    /**
     * Handle the specified exception.
     * 
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @param t the exception thrown
     * @return true if the exception was handled, false otherwise
     */
    public boolean handleException(CommandSender sender, org.bukkit.command.Command command, String label, String[] args, Throwable t);

}
