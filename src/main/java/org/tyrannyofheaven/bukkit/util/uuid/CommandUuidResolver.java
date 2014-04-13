/*
 * Copyright 2014 ZerothAngel <zerothangel@tyrannyofheaven.org>
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
package org.tyrannyofheaven.bukkit.util.uuid;

import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.colorize;
import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.sendMessage;
import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.hasText;
import static org.tyrannyofheaven.bukkit.util.command.reader.CommandReader.abortBatchProcessing;
import static org.tyrannyofheaven.bukkit.util.command.reader.CommandReader.isBatchProcessing;
import static org.tyrannyofheaven.bukkit.util.uuid.UuidUtils.parseUuidDisplayName;

import java.util.UUID;
import java.util.concurrent.Executor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandUuidResolver {

    private final Plugin plugin;

    private final UuidResolver uuidResolver;

    private final Executor executor;

    private final boolean abortInline;

    public CommandUuidResolver(Plugin plugin, UuidResolver uuidResolver, Executor executor, boolean abortInline) {
        this.plugin = plugin;
        this.uuidResolver = uuidResolver;
        this.executor = executor;
        this.abortInline = abortInline;
    }

    public void resolveUsername(CommandSender sender, String name, boolean skip, boolean forceInline, CommandUuidResolverHandler handler) {
        if (sender == null)
            throw new IllegalArgumentException("sender cannot be null");
        if (handler == null)
            throw new IllegalArgumentException("handler cannot be null");

        if (skip || name == null) {
            // Simple case: no need to resolve because skip is true or name is null, run inline
            handler.process(sender, name, null, skip);
        }
        else {
            // See if it's UUID or UUID/DisplayName
            UuidDisplayName udn = parseUuidDisplayName(name);
            if (udn != null) {
                String displayName;
                OfflinePlayer player = Bukkit.getOfflinePlayer(udn.getUuid());
                if (player != null && player.getName() != null) {
                    // Use last known name
                    displayName = player.getName();
                }
                else {
                    // Default display name (either what was passed in or the UUID in string form)
                    displayName = hasText(udn.getDisplayName()) ? udn.getDisplayName() : udn.getUuid().toString();
                }
                handler.process(sender, displayName, udn.getUuid(), skip);
            }
            else {
                // Is the named player online?
                Player player = Bukkit.getPlayerExact(name);
                if (player != null) {
                    // Simply run inline, no explicit lookup necessary
                    handler.process(sender, player.getName(), player.getUniqueId(), skip);
                }
                else if (forceInline) {
                    // Lookup & run inline
                    udn = uuidResolver.resolve(name);
                    if (udn == null) {
                        fail(sender, name);
                    }
                    else {
                        handler.process(sender, udn.getDisplayName(), udn.getUuid(), skip);
                    }
                }
                else {
                    // Check if cached by resolver
                    udn = uuidResolver.resolve(name, true);
                    if (udn != null) {
                        // If so, run inline
                        handler.process(sender, udn.getDisplayName(), udn.getUuid(), skip);
                    }
                    else {
                        // As an absolute last resort, resolve and run async
                        sendMessage(sender, colorize("{GRAY}(Resolving UUID...)"));
                        Runnable task = new UsernameResolverHandlerRunnable(this, plugin, uuidResolver, sender, name, skip, handler);
                        // NB Bukkit#getOfflinePlayer(String) provides almost the same service
                        // However, it's not known whether it is fully thread-safe.
                        executor.execute(task);
                    }
                }
            }
        }
    }

    private static class UsernameResolverHandlerRunnable implements Runnable {

        private final CommandUuidResolver commandUuidResolver;

        private final Plugin plugin;
        
        private final UuidResolver uuidResolver;

        private final CommandSender sender;

        private final UUID senderUuid;

        private final String name;

        private final boolean skip;

        private final CommandUuidResolverHandler handler;

        private UsernameResolverHandlerRunnable(CommandUuidResolver commandUuidResolver, Plugin plugin, UuidResolver uuidResolver, CommandSender sender, String name, boolean skip, CommandUuidResolverHandler handler) {
            this.commandUuidResolver = commandUuidResolver;
            this.plugin = plugin;
            this.uuidResolver = uuidResolver;
            this.sender = sender instanceof Player ? null : sender;
            this.senderUuid = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
            this.name = name;
            this.skip = skip;
            this.handler = handler;
        }

        private CommandSender getSender() {
            return sender;
        }

        @Override
        public void run() {
            // Perform lookup
            final UuidDisplayName udn = uuidResolver.resolve(name);

            // Run the rest in the main thread
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    // Re-lookup sender
                    CommandSender sender = getSender() != null ? getSender() : Bukkit.getPlayer(senderUuid);

                    // Only execute if sender is still around
                    if (sender != null) {
                        if (udn == null) {
                            commandUuidResolver.fail(sender, name);
                        }
                        else {
                            handler.process(sender, udn.getDisplayName(), udn.getUuid(), skip);
                        }
                    }
                }
            });
        }

    }

    public void resolveUsername(CommandSender sender, String name, boolean skip, CommandUuidResolverHandler handler) {
        resolveUsername(sender, name, skip, isBatchProcessing(), handler);
    }

    public void resolveUsername(CommandSender sender, String name, CommandUuidResolverHandler handler) {
        resolveUsername(sender, name, false, isBatchProcessing(), handler);
    }

    private void fail(CommandSender sender, String name) {
        sendMessage(sender, colorize("{RED}Failed to lookup UUID for {AQUA}%s"), name);
        if (abortInline) abortBatchProcessing();
    }

}
