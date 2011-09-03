package org.tyrannyofheaven.bukkit.util.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.permissions.Permissible;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils;

public final class InvocationChain {

    private final List<CommandInvocation> chain;

    private InvocationChain(List<CommandInvocation> chain) {
        this.chain = chain;
    }

    InvocationChain() {
        this(new ArrayList<CommandInvocation>());
    }

    void addInvocation(String label, CommandMetaData commandMetaData) {
        chain.add(new CommandInvocation(label, commandMetaData));
    }

    // Generate a usage string
    public String getUsageString() {
        boolean first = true;
        
        StringBuilder usage = new StringBuilder();
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
                    usage.append('[');
                    usage.append(omd.getName());
                    if (omd.getType() != Boolean.class && omd.getType() != Boolean.TYPE) {
                        // Show a value
                        usage.append(" <");
                        usage.append(omd.getValueName());
                        usage.append('>');
                    }
                    usage.append(']');
                    if (j.hasNext())
                        usage.append(' ');
                }
                
                for (Iterator<OptionMetaData> j = cmd.getPositionalArguments().iterator(); j.hasNext();) {
                    OptionMetaData omd = j.next();
                    if (omd.isOptional())
                        usage.append('[');
                    usage.append('<');
                    usage.append(omd.getName());
                    usage.append('>');
                    if (omd.isOptional())
                        usage.append(']');
                    if (j.hasNext())
                        usage.append(' ');
                }
            }
            
            if (i.hasNext())
                usage.append(' ');
        }
        return usage.toString();
    }

    public boolean canBeExecutedBy(Permissible permissible) {
        for (CommandInvocation ci : chain) {
            if (!PermissionUtils.hasPermissions(permissible, ci.getCommandMetaData().isRequireAll(), ci.getCommandMetaData().getPermissions()))
                return false;
        }
        return true;
    }

    InvocationChain copy() {
        // Feh to clone()
        return new InvocationChain(new ArrayList<CommandInvocation>(chain));
    }

}
