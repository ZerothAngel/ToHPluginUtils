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
 * A parameter that receives a value from the current CommandSession.
 * 
 * @author zerothangel
 */
final class SessionParameter implements MethodParameter {

    private final String name;

    private final Class<?> type;

    SessionParameter(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * The name of the CommandSession value.
     * 
     * @return the CommandSession value name
     */
    String getName() {
        return name;
    }

    /**
     * The type of this parameter.
     * 
     * @return the parameter type
     */
    Class<?> getType() {
        return type;
    }
    
}
