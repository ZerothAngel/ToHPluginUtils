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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for a command.
 * 
 * @author zerothangel
 */
final class CommandMetaData {

    private final Object handler;

    private final Method method;

    private final List<MethodParameter> parameters;

    private final String[] permissions;

    private final boolean requireAll;
    
    private final List<OptionMetaData> flagOptions;
    
    private final List<OptionMetaData> positionalArguments;

    /**
     * Create a CommandMetaData with the given arguments.
     * 
     * @param handler the handler object
     * @param method the associated method in the handler object
     * @param options method parameters
     * @param permissions required permissions, if any
     * @param requireAll true if all permissions are required
     */
    public CommandMetaData(Object handler, Method method, List<MethodParameter> options, String[] permissions, boolean requireAll) {
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");
        if (method == null)
            throw new IllegalArgumentException("method cannot be null");
        
        if (options == null)
            options = Collections.emptyList();
        if (permissions == null)
            permissions = new String[0];

        this.handler = handler;
        this.method = method;
        this.permissions = Arrays.copyOf(permissions, permissions.length);
        this.requireAll = requireAll;

        this.parameters = Collections.unmodifiableList(new ArrayList<MethodParameter>(options));
        
        List<OptionMetaData> flagOptions = new ArrayList<OptionMetaData>();
        List<OptionMetaData> positionalArguments = new ArrayList<OptionMetaData>();
        for (MethodParameter mp : this.parameters) {
            if (mp instanceof OptionMetaData) {
                OptionMetaData omd = (OptionMetaData)mp;
                if (omd.isArgument()) {
                    positionalArguments.add(omd);
                }
                else {
                    flagOptions.add(omd);
                }
            }
        }
        
        this.flagOptions = Collections.unmodifiableList(flagOptions);
        this.positionalArguments = Collections.unmodifiableList(positionalArguments);
    }

    /**
     * Return the method parameter metadata.
     * 
     * @return list of MethodParameters
     */
    public List<MethodParameter> getParameters() {
        return parameters;
    }

    /**
     * Return metadata for any flags.
     * 
     * @return set of OptionMetaData for any associated flags
     */
    public List<OptionMetaData> getFlagOptions() {
        return flagOptions;
    }

    /**
     * Return metadata for any positional arguments.
     * 
     * @return list of OptionMetaData for any positional arguments
     */
    public List<OptionMetaData> getPositionalArguments() {
        return positionalArguments;
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
