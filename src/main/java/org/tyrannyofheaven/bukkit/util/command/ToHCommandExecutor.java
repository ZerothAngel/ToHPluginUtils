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

import static org.tyrannyofheaven.bukkit.util.ToHLoggingUtils.error;
import static org.tyrannyofheaven.bukkit.util.ToHLoggingUtils.warn;
import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.sendMessage;
import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.hasText;
import static org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils.displayPermissionException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.util.ToHStringUtils;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionException;

/**
 * A Bukkit CommandExecutor implementation that ties everything together.
 * 
 * @author zerothangel
 */
public class ToHCommandExecutor<T extends Plugin> implements TabExecutor {

    private final T plugin;

    private final HandlerExecutor<T> rootHandlerExecutor;

    private UsageOptions usageOptions = new DefaultUsageOptions();

    private Map<String, TypeCompleter> typeCompleterRegistry = new HashMap<String, TypeCompleter>();

    /**
     * Create an instance.
     * 
     * @param plugin the associated plugin
     * @param handlers the handler objects
     */
    public ToHCommandExecutor(T plugin, Object... handlers) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");

        this.plugin = plugin;

        rootHandlerExecutor = new HandlerExecutor<T>(plugin, usageOptions, handlers);
        
        // Register default TypeCompleters
        registerTypeCompleter("constant", new ConstantTypeCompleter());
        registerTypeCompleter("player", new PlayerTypeCompleter());
        registerTypeCompleter("world", new WorldTypeCompleter());
    }

    /**
     * Register top-level commands with the server.
     */
    public void registerCommands() {
        rootHandlerExecutor.registerCommands(this);
    }

    public ToHCommandExecutor<T> registerTypeCompleter(String name, TypeCompleter typeCompleter) {
        if (!ToHStringUtils.hasText(name))
            throw new IllegalArgumentException("name must have a value");
        if (typeCompleter == null)
            throw new IllegalArgumentException("typeCompleter cannot be null");
        
        typeCompleterRegistry.put(name, typeCompleter);
        return this;
    }

    public void setUsageOptions(UsageOptions usageOptions) {
        if (usageOptions == null)
            throw new IllegalArgumentException("usageOptions cannot be null");
        
        this.usageOptions = usageOptions;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        InvocationChain invChain = new InvocationChain();

        try {
            // NB: We use command.getName() rather than label. This allows the
            // user to freely add aliases by editing plugin.yml. However,
            // this also makes aliases in @Command mostly useless.
            rootHandlerExecutor.execute(sender, command.getName(), label, args, invChain, new CommandSession());
            return true;
        }
        catch (PermissionException e) {
            displayPermissionException(sender, e);
            return true;
        }
        catch (ParseException e) {
            // Show message if one was given
            if (hasText(e.getMessage()))
                sendMessage(sender, "%s%s", ChatColor.RED, e.getMessage());
            sendMessage(sender, invChain.getUsageString(usageOptions));
            return true;
        }
        catch (Error e) {
            // Re-throw Errors
            throw e;
        }
        catch (Throwable t) {
            sendMessage(sender, ChatColor.RED + "Plugin error; see server log.");
            error(plugin, "Command handler exception:", t);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            return rootHandlerExecutor.getTabCompletions(sender, command.getName(), alias, args, null, null, typeCompleterRegistry);
        }
        catch (Error e) {
            throw e;
        }
        catch (Throwable t) {
            warn(plugin, "Tab completion exception:", t);
            return Collections.emptyList();
        }
    }

}
