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

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Metadata for sub-commands. Also serves as the base class for
 * CommandMetaData.
 * 
 * @author asaddi
 */
class SubCommandMetaData {

    private final Object handler;

    private final Method method;

    private final String[] permissions;
    
    private final boolean requireAll;

    /**
     * Create a SubCommandMetaData instance with the given arguments.
     * 
     * @param handler the handler object
     * @param method the command method within the handler object
     * @param permissions the names of any required permissions
     * @param requireAll true if all permissions are required
     */
    protected SubCommandMetaData(Object handler, Method method, String[] permissions, boolean requireAll) {
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");
        if (method == null)
            throw new IllegalArgumentException("method cannot be null");
        
        if (permissions == null)
            permissions = new String[0];

        this.handler = handler;
        this.method = method;
        this.permissions = Arrays.copyOf(permissions, permissions.length);
        this.requireAll = requireAll;
    }

    /**
     * Returns the handler object.
     * 
     * @return the handler object
     */
    public Object getHandler() {
        return handler;
    }

    /**
     * Returns the handler method.
     * 
     * @return the handler method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the permissions, if any.
     * 
     * @return an array of 0 or more permission names. Will never be
     *   <code>null</code>.
     */
    public String[] getPermissions() {
        return permissions;
    }

    /**
     * Returns whether or not all permissions are required.
     * 
     * @return true if all permissions are required
     */
    public boolean isRequireAll() {
        return requireAll;
    }

}
