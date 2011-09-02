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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Metadata for a command.
 * 
 * @author asaddi
 */
class CommandMetaData extends SubCommandMetaData {

    private final List<MethodParameter> parameters;

    private final Set<OptionMetaData> flagOptions;
    
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
        super(handler, method, permissions, requireAll);

        if (options == null)
            options = Collections.emptyList();

        this.parameters = Collections.unmodifiableList(new ArrayList<MethodParameter>(options));
        
        Set<OptionMetaData> flagOptions = new HashSet<OptionMetaData>();
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
        
        this.flagOptions = Collections.unmodifiableSet(flagOptions);
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
    public Set<OptionMetaData> getFlagOptions() {
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

}
