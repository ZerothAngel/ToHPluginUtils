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

/**
 * Holder for a command invocation. Used for generating the usage string.
 * 
 * @author zerothangel
 */
final class CommandInvocation {

    private final String label;
    
    private final CommandMetaData commandMetaData;

    CommandInvocation(String label, CommandMetaData commandMetaData) {
        this.label = label;
        this.commandMetaData = commandMetaData;
    }

    /**
     * Returns the name of the command. May be an alias.
     * 
     * @return the command label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the associated CommandMetaData.
     * 
     * @return the CommandMetaData
     */
    public CommandMetaData getCommandMetaData() {
        return commandMetaData;
    }

}
