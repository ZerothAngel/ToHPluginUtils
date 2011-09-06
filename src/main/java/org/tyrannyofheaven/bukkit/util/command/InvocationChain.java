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

import static org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils.hasPermissions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.permissions.Permissible;

/**
 * Holds a chain of CommandInvocations. Used for generating the usage string.
 * 
 * @author zerothangel
 */
final class InvocationChain {

    private final List<CommandInvocation> chain;

    private InvocationChain(List<CommandInvocation> chain) {
        this.chain = chain;
    }

    InvocationChain() {
        this(new LinkedList<CommandInvocation>());
    }

    // Adds a new invocation to the chain
    void addInvocation(String label, CommandMetaData commandMetaData) {
        chain.add(new CommandInvocation(label, commandMetaData));
    }

    // Generate a usage string
    String getUsageString(UsageOptions usageOptions) {
        return getUsageString(usageOptions, false);
    }

    // Generate a usage string
    String getUsageString(UsageOptions usageOptions, boolean withDescription) {
        boolean first = true;
        
        StringBuilder usage = new StringBuilder();
        usage.append(usageOptions.getPrefix());
        for (Iterator<CommandInvocation> i = chain.iterator(); i.hasNext();) {
            CommandInvocation ci = i.next();
            if (first) {
                usage.append('/');
                first = false;
            }
            usage.append(ci.getLabel());
            
            CommandMetaData cmd = ci.getCommandMetaData();
            if (!cmd.getFlagOptions().isEmpty() || !cmd.getPositionalArguments().isEmpty()) {
                usage.append(' ');
                
                for (Iterator<OptionMetaData> j = cmd.getFlagOptions().iterator(); j.hasNext();) {
                    OptionMetaData omd = j.next();
                    usage.append(usageOptions.getFlagStart());
                    usage.append(omd.getName());
                    if (omd.getType() != Boolean.class && omd.getType() != Boolean.TYPE) {
                        // Show a value
                        usage.append(usageOptions.getFlagValueStart());
                        usage.append(omd.getValueName());
                        usage.append(usageOptions.getFlagValueEnd());
                    }
                    usage.append(usageOptions.getFlagEnd());
                    if (j.hasNext())
                        usage.append(' ');
                }
                
                if (!cmd.getFlagOptions().isEmpty() && !cmd.getPositionalArguments().isEmpty())
                    usage.append(' ');

                for (Iterator<OptionMetaData> j = cmd.getPositionalArguments().iterator(); j.hasNext();) {
                    OptionMetaData omd = j.next();
                    usage.append(usageOptions.getParameterStart(omd.isOptional()));
                    usage.append(omd.getName());
                    usage.append(usageOptions.getParameterEnd(omd.isOptional()));
                    if (j.hasNext())
                        usage.append(' ');
                }
            }
            
            if (i.hasNext())
                usage.append(' ');
        }

        // Attach description
        if (withDescription && !chain.isEmpty()) {
            // Pull out last CommandMetaData
            CommandMetaData cmd = chain.get(chain.size() - 1).getCommandMetaData();
            if (cmd.getDescription() != null) {
                usage.append(usageOptions.getDescriptionDelimiter());
                usage.append(cmd.getDescription());
            }
        }
        
        return usage.toString();
    }

    // Tests whether the given permissible can execute this entire chain
    boolean canBeExecutedBy(Permissible permissible) {
        for (CommandInvocation ci : chain) {
            if (!hasPermissions(permissible, ci.getCommandMetaData().isRequireAll(), ci.getCommandMetaData().getPermissions()))
                return false;
        }
        return true;
    }

    // Returns a copy of this chain
    InvocationChain copy() {
        // Feh to clone()
        return new InvocationChain(new LinkedList<CommandInvocation>(chain));
    }

    void pop() {
        if (chain.isEmpty())
            throw new IllegalStateException("InvocationChain is empty");
        chain.remove(chain.size() - 1);
    }

}
