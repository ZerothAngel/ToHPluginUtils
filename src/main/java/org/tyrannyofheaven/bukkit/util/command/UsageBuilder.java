package org.tyrannyofheaven.bukkit.util.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class UsageBuilder<T extends Plugin> {

    private final HandlerExecutor<T> handlerExecutor;

    private final CommandSender sender;

    private final InvocationChain rootInvocationChain;

    private final List<String> outputLines = new ArrayList<String>();

    UsageBuilder(HandlerExecutor<T> handlerExecutor, CommandSender sender, InvocationChain rootInvocationChain) {
        if (handlerExecutor == null)
            throw new IllegalArgumentException("handlerExecutor cannot be null");
        if (sender == null)
            throw new IllegalArgumentException("sender cannot be null");
        if (rootInvocationChain == null)
            throw new IllegalArgumentException("rootInvocationChain cannot be null");
        
        this.handlerExecutor = handlerExecutor;
        this.sender = sender;
        this.rootInvocationChain = rootInvocationChain;
    }

    public UsageBuilder<T> forHandlerAndCommand(Object handler, String command, boolean usePermissions) {
        HandlerExecutor<T> he = handlerExecutor.handlerExecutorFor(handler);
        InvocationChain invChain = he.fillInvocationChain(rootInvocationChain.copy(), command);
        if (!usePermissions || invChain.canBeExecutedBy(sender)) {
            outputLines.add(invChain.getUsageString());
        }
        return this;
    }

    public UsageBuilder<T> forHandlerAndCommand(Object handler, String command) {
        return forHandlerAndCommand(handler, command, true);
    }

    public void show() {
        for (String line : outputLines) {
            sender.sendMessage(line);
        }
    }

}
