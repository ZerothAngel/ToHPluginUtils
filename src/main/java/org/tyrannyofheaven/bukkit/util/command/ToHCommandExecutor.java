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

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionException;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils;

/**
 * A Bukkit CommandExecutor implementation that ties everything together.
 * 
 * @author asaddi
 */
public class ToHCommandExecutor<T extends Plugin> implements CommandExecutor {

    private final T plugin;

    private final HandlerExecutor<T> rootHandlerExecutor;

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

        rootHandlerExecutor = new HandlerExecutor<T>(plugin, handlers);
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            // NB: We use command.getName() rather than label. This allows the
            // user to freely add aliases by editing plugin.yml. However,
            // this also makes aliases in @Command and @SubCommand useless.
            return rootHandlerExecutor.execute(sender, command.getName(), args);
        }
        catch (PermissionException e) {
            PermissionUtils.displayPermissionException(sender, e);
            return true;
        }
        catch (ParseException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            return false;
        }
        catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Plugin error; see server log");
            plugin.getServer().getLogger().log(Level.SEVERE, String.format("[%s] Unhandled exception:", plugin.getDescription().getName()), e);
            return true;
        }
    }

}
