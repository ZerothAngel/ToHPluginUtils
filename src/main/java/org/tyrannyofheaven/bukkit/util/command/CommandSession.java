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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides an easy method to pass values between handlers without having to
 * instantiate a new handler for each invocation or break thread safety.
 * 
 * @author asaddi
 */
public final class CommandSession {

    private final Map<String, Object> data = new HashMap<String, Object>();
    
    /**
     * Retrieves a session value.
     * 
     * @param name the name of the value
     * @return the value or null
     */
    public Object getValue(String name) {
        return data.get(name);
    }

    /**
     * Retrieves a session value. Throws an exception if the value is not found
     * or is of the wrong type.
     * 
     * @param <T> the value type
     * @param name the name of the value
     * @param clazz the expected value type
     * @return the value
     */
    public <T> T getValue(String name, Class<T> clazz) {
        Object obj = data.get(name);
        if (obj != null) {
            if (clazz.isAssignableFrom(obj.getClass())) {
                @SuppressWarnings("unchecked")
                T o = (T)obj;
                return o;
            }
            else {
                throw new IllegalArgumentException(String.format("Session value '%s' not assignable from type %s", name, clazz));
            }
        }
        throw new IllegalArgumentException(String.format("Missing session value '%s'", name));
    }

    /**
     * Sets a session value.
     * 
     * @param name the name of the value
     * @param value the value to set
     */
    public void setValue(String name, Object value) {
        data.put(name, value);
    }

    /**
     * Removes a session value.
     * 
     * @param name the name of the value
     */
    public void removeValue(String name) {
        data.remove(name);
    }

}
