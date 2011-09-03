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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Automatically generates a help page for a sub-command.
 * 
 * @author asaddi
 *
 * @param <T>
 */
public class HelpBuilder<T extends Plugin> {

    private final HandlerExecutor<T> handlerExecutor;

    private final InvocationChain rootInvocationChain;

    private CommandSender sender;

    private Object handler;

    private final List<String> outputLines = new ArrayList<String>();

    HelpBuilder(HandlerExecutor<T> handlerExecutor, InvocationChain rootInvocationChain) {
        if (handlerExecutor == null)
            throw new IllegalArgumentException("handlerExecutor cannot be null");
        if (rootInvocationChain == null)
            throw new IllegalArgumentException("rootInvocationChain cannot be null");
        
        this.handlerExecutor = handlerExecutor;
        this.rootInvocationChain = rootInvocationChain;
    }

    private CommandSender getCommandSender() {
        if (sender == null)
            throw new IllegalStateException("CommandSender has not been set");
        return sender;
    }

    public HelpBuilder<T> withCommandSender(CommandSender sender) {
        if (sender == null)
            throw new IllegalArgumentException("sender cannot be null");

        if (this.sender != null)
            throw new IllegalStateException("CommandSender has already been set");
        this.sender = sender;
        return this;
    }

    public HelpBuilder<T> withHandler(Object handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");
        
        this.handler = handler;
        return this;
    }

    private Object getHandler() {
        if (handler == null)
            throw new IllegalStateException("handler has not been set");
        return handler;
    }

    public HelpBuilder<T> forHandlerAndCommand(Object handler, String command, boolean usePermissions) {
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");
        if (command == null || command.trim().length() == 0)
            throw new IllegalArgumentException("command must have a value");

        HandlerExecutor<T> he = handlerExecutor.handlerExecutorFor(handler);
        InvocationChain invChain = he.fillInvocationChain(rootInvocationChain.copy(), command);
        if (!usePermissions || invChain.canBeExecutedBy(getCommandSender())) {
            outputLines.add(invChain.getUsageString());
        }
        return this;
    }

    public HelpBuilder<T> forHandlerAndCommand(Object handler, String command) {
        return forHandlerAndCommand(handler, command, true);
    }

    public HelpBuilder<T> forCommand(String command, boolean usePermissions) {
        return forHandlerAndCommand(getHandler(), command, usePermissions);
    }

    public HelpBuilder<T> forCommand(String command) {
        return forCommand(command, true);
    }

    public void show() {
        for (String line : outputLines) {
            getCommandSender().sendMessage(line);
        }
    }

    public String[] getLines() {
        return outputLines.toArray(new String[outputLines.size()]);
    }

}
