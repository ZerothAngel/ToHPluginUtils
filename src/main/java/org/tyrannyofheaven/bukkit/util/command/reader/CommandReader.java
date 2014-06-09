package org.tyrannyofheaven.bukkit.util.command.reader;

import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.colorize;
import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.sendMessage;
import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.delimitedString;
import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.hasText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to read a file containing commands and execute them.
 * 
 * @author zerothangel
 */
public class CommandReader {

    // Used to hold the batch processing abort flag
    private static final ThreadLocal<Boolean> abortFlags = new ThreadLocal<>();

    /**
     * Execute commands from a file. Commands will be echoed back to the sender.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param file the file to read commands from
     * @param plugins Zero or more plugins to restrict the commands to
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, File file, Plugin... plugins) throws IOException {
        return read(server, sender, file, true, plugins);
    }
    
    /**
     * Execute commands from a stream. Commands will be echoed back to the sender.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param input InputStream for commands
     * @param plugins Zero or more plugins to restrict the commands to
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, InputStream input, Plugin...plugins) throws IOException {
        return read(server, sender, input, true, plugins);
    }

    /**
     * Execute commands from a file.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param file the file to read commands from
     * @param echo true if commands should be echoed back to sender
     * @param plugins Zero or more plugins to restrict the commands to
     * @return true if all commands executed successfully
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, File file, boolean echo, Plugin... plugins) throws IOException {
        return read(server, sender, new FileInputStream(file), echo, plugins);
    }

    private static Command getCommand(Server server, String name, Plugin... plugins) {
        name = name.toLowerCase();

        PluginCommand command = server.getPluginCommand(name);
        
        if (plugins == null || plugins.length == 0) {
            // No restrictions
            return command;
        }
        
        // Find a match among specified plugins
        for (Plugin plugin : plugins) {
            // Same logic as JavaPlugin#getCommand()
            if (command != null && command.getPlugin() != plugin)
                command = server.getPluginCommand(String.format("%s:%s", plugin.getDescription().getName(), name));
            
            if (command != null && command.getPlugin() == plugin)
                return command;
        }
        
        return null;
    }

    /**
     * Execute commands from a stream.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param input InputStream for commands
     * @param echo true if commands should be echoed back to sender
     * @param plugins Zero or more plugins to restrict the commands to
     * @return true if all commands executed successfully
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, InputStream input, boolean echo, Plugin... plugins) throws IOException {
        List<CommandCall> calls = new ArrayList<>();

        // Read entire stream before executing anything
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        try {
            int lineNo = 0;
            String line;
            while ((line = in.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) {
                    // Skip comments and blank lines
                    continue;
                }
                
                // Break up into args
                String[] args = line.split(" ");

                // Strip leading slash if present
                String c = args[0].toLowerCase();
                if (c.startsWith("/"))
                    c = c.substring(1);
                
                Command command = getCommand(server, c, plugins);
                if (command == null) {
                    throw new CommandReaderException(String.format("Unknown command at line %d", lineNo));
                }
                
                calls.add(new CommandCall(command, c, Arrays.copyOfRange(args, 1, args.length)));
            }
        }
        finally {
            in.close();
        }
        
        // Set up abort flag
        abortFlags.set(Boolean.FALSE);

        try {
            // Execute each call
            for (CommandCall call : calls) {
                try {
                    if (echo) {
                        sendMessage(sender, colorize("{GRAY}%s%s%s%s"),
                                (sender instanceof Player ? "/" : ""),
                                call.getAlias(),
                                (call.getArgs().length > 0 ? " " : ""),
                                delimitedString(" ", (Object[])call.getArgs()));
                    }
                    if (!call.getCommand().execute(sender, call.getAlias(), call.getArgs()))
                        return false;
                    
                    // Check aborting
                    if (abortFlags.get() != null && abortFlags.get())
                        return false;
                }
                catch (Error e) {
                    throw e;
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Throwable t) {
                    throw new CommandReaderException(t);
                }
            }
        }
        finally {
            // Remove ThreadLocal to prevent memory leaks
            abortFlags.remove();
        }

        return true;
    }

    /**
     * May be called by command handlers to abort batch processing. Does nothing
     * if the handler was not called within {@link #read(Server, CommandSender, InputStream, boolean, Plugin...)}.
     */
    public static void abortBatchProcessing() {
        if (isBatchProcessing())
            abortFlags.set(Boolean.TRUE);
    }

    /**
     * Tests if current thread is currently running batch commands, e.g.
     * called within {@link #read(Server, CommandSender, InputStream, boolean, Plugin...)}.
     * 
     * @return true if the current thread is running batch commands
     */
    public static boolean isBatchProcessing() {
        return abortFlags.get() != null;
    }

    // Holder for command invocation
    private static class CommandCall {
        
        private final Command command;
        
        private final String alias;
        
        private final String[] args;
        
        public CommandCall(Command command, String alias, String[] args) {
            if (command == null)
                throw new IllegalArgumentException("command cannot be null");
            if (!hasText(alias))
                throw new IllegalArgumentException("alias must have a value");
            if (args == null)
                args = new String[0];
            this.command = command;
            this.alias = alias;
            this.args = args;
        }

        public Command getCommand() {
            return command;
        }

        public String getAlias() {
            return alias;
        }

        public String[] getArgs() {
            return Arrays.copyOf(args, args.length);
        }
        
    }

}
