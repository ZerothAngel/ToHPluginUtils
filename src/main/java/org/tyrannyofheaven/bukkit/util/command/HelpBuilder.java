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

import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.hasText;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Automatically generates a help page for a sub-command.
 * 
 * @author asaddi
 *
 * @param <T>
 */
public class HelpBuilder {

    private final HandlerExecutor<?> handlerExecutor;

    private final InvocationChain rootInvocationChain;

    private UsageOptions usageOptions;

    private CommandSender sender;

    private Object handler;

    private final List<String> outputLines = new ArrayList<String>();

    HelpBuilder(HandlerExecutor<?> handlerExecutor, InvocationChain rootInvocationChain, UsageOptions usageOptions) {
        if (handlerExecutor == null)
            throw new IllegalArgumentException("handlerExecutor cannot be null");
        if (rootInvocationChain == null)
            throw new IllegalArgumentException("rootInvocationChain cannot be null");
        if (usageOptions == null)
            throw new IllegalArgumentException("usageOptions cannot be null");
        
        this.handlerExecutor = handlerExecutor;
        this.rootInvocationChain = rootInvocationChain;
        this.usageOptions = usageOptions;
    }

    /**
     * Change UsageOptions. By default, the ones in {@link ToHCommandExecutor}
     * are used.
     * 
     * @param usageOptions UsageOptions to use
     * @return this HelpBuilder
     */
    public HelpBuilder withUsageOptions(UsageOptions usageOptions) {
        if (usageOptions == null)
            throw new IllegalArgumentException("usageOptions cannot be null");
        
        this.usageOptions = usageOptions;
        return this;
    }

    // Retrives CommandSender, complaining if one hasn't been set yet
    private CommandSender getCommandSender() {
        if (sender == null)
            throw new IllegalStateException("CommandSender has not been set");
        return sender;
    }

    /**
     * Set the CommandSender. This can only be called once.
     * 
     * @param sender the CommandSender
     * @return this HelpBuilder
     */
    public HelpBuilder withCommandSender(CommandSender sender) {
        if (sender == null)
            throw new IllegalArgumentException("sender cannot be null");

        if (this.sender != null)
            throw new IllegalStateException("CommandSender has already been set");
        this.sender = sender;
        return this;
    }

    /**
     * Set the current handler object.
     * 
     * @param handler the handler object
     * @return this HelpBuilder
     */
    public HelpBuilder withHandler(Object handler) {
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");
        
        this.handler = handler;
        return this;
    }

    // Retrieve the current handler object, complaining if one hasn't been set
    private Object getHandler() {
        if (handler == null)
            throw new IllegalStateException("handler has not been set");
        return handler;
    }

    /**
     * Generate a usage message for a particular sibling command.
     * 
     * @param command the sibling command
     * @param usePermissions true if permissions should be checked. If the current
     *   sender fails the check, no usage is generated.
     * @return this HelpBuilder
     */
    public HelpBuilder forSiblingCommand(String command, boolean usePermissions) {
        if (!hasText(command))
            throw new IllegalArgumentException("command must have a value");
        
        // Remove last invocation (from a copy)
        InvocationChain invChain = rootInvocationChain.copy();
        invChain.pop();
        // Fill with sibling invocation
        handlerExecutor.fillInvocationChain(invChain, command);
        if (!usePermissions || invChain.canBeExecutedBy(getCommandSender())) {
            outputLines.add(invChain.getUsageString(usageOptions, true));
        }
        return this;
    }

    /**
     * Generate a usage message for a particular sibling command. Permissions are used.
     * 
     * @param command the sibling command
     * @param usePermissions true if permissions should be checked. If the current
     *   sender fails the check, no usage is generated.
     * @return this HelpBuilder
     */
    public HelpBuilder forSiblingCommand(String command) {
        return forSiblingCommand(command, true);
    }

    /**
     * Generate a usage message for a particular sub-command.
     * 
     * @param handler the handler in which the command resides
     * @param command the command
     * @param usePermissions true if permissions should be checked. If the current
     *   sender fails the check, no usage is generated.
     * @return this HelpBuilder
     */
    public HelpBuilder forHandlerAndCommand(Object handler, String command, boolean usePermissions) {
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");
        if (!hasText(command))
            throw new IllegalArgumentException("command must have a value");

        HandlerExecutor<?> he = handlerExecutor.handlerExecutorFor(handler);
        InvocationChain invChain = rootInvocationChain.copy();
        he.fillInvocationChain(invChain, command);
        if (!usePermissions || invChain.canBeExecutedBy(getCommandSender())) {
            outputLines.add(invChain.getUsageString(usageOptions, true));
        }
        return this;
    }

    /**
     * Generate a usage message for a particular sub-command. Permissions are used.
     * 
     * @param handler the handler in which the command resides
     * @param command the command
     * @return this HelpBuilder
     */
    public HelpBuilder forHandlerAndCommand(Object handler, String command) {
        return forHandlerAndCommand(handler, command, true);
    }

    /**
     * Generate a usage message for a particular sub-command. The current handler
     * object is referenced.
     * 
     * @param command the command
     * @param usePermissions true if permissions should be checked. If the current
     *   sender fails the check, no usage is generated.
     * @return this HelpBuilder
     */
    public HelpBuilder forCommand(String command, boolean usePermissions) {
        return forHandlerAndCommand(getHandler(), command, usePermissions);
    }

    /**
     * Generate a usage message for a particular sub-command. The current handler
     * object is referenced. Permissions are used.
     * 
     * @param command the command
     * @return this HelpBuilder
     */
    public HelpBuilder forCommand(String command) {
        return forCommand(command, true);
    }

    /**
     * Outputs all sub-command usage messages.
     */
    public void show() {
        for (String line : outputLines) {
            getCommandSender().sendMessage(line);
        }
    }

    /**
     * Retrieves the raw sub-command usage messages.
     * 
     * @return the sub-command usage messages
     */
    public String[] getLines() {
        return outputLines.toArray(new String[outputLines.size()]);
    }

}
