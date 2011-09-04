/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
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
 * One of the special parameters types that automatically receive a value
 * from the framework.
 * 
 * @author asaddi
 */
final class SpecialParameter implements MethodParameter {

    private final Type type;
    
    /**
     * Create a SpecialParameter instance.
     * 
     * @param type the special type of parameter
     */
    SpecialParameter(Type type) {
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

        this.type = type;
    }

    /**
     * Retrieve the SpecialParameter type.
     * 
     * @return the type
     */
    Type getType() {
        return type;
    }

    enum Type {
        /**
         * org.bukkit.Server
         */
        SERVER,
        
        /**
         * org.bukkit.plugin.Plugin or subclasses
         */
        PLUGIN,
        
        /**
         * org.bukkit.plugin.CommandSender
         */
        COMMAND_SENDER,

        /**
         * Unannotated String parameter
         */
        LABEL,

        /**
         * UsageBuilder
         */
        USAGE_BUILDER,

        /**
         * Session
         */
        SESSION,

        /**
         * String[] parameter
         */
        REST;
    }

}
