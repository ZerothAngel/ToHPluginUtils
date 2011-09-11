package org.tyrannyofheaven.bukkit.util.command.reader;

import static org.tyrannyofheaven.bukkit.util.ToHUtils.colorize;
import static org.tyrannyofheaven.bukkit.util.ToHUtils.delimitedString;
import static org.tyrannyofheaven.bukkit.util.ToHUtils.hasText;
import static org.tyrannyofheaven.bukkit.util.ToHUtils.sendMessage;

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
import org.bukkit.entity.Player;

/**
 * Utility class to read a file containing commands and execute them.
 * 
 * @author asaddi
 */
public class CommandReader {

    /**
     * Execute commands from a file. Commands will be echoed back to the sender.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param file the file to read commands from
     * @param echo true if commands should be echoed back to sender
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, File file) throws IOException {
        return read(server, sender, file, true);
    }
    
    /**
     * Execute commands from a stream. Commands will be echoed back to the sender.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param input InputStream for commands
     * @param echo true if commands should be echoed back to sender
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, InputStream input) throws IOException {
        return read(server, sender, input, true);
    }

    /**
     * Execute commands from a file.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param file the file to read commands from
     * @param echo true if commands should be echoed back to sender
     * @return true if all commands executed successfully
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, File file, boolean echo) throws IOException {
        return read(server, sender, new FileInputStream(file), echo);
    }

    /**
     * Execute commands from a stream.
     * 
     * @param server the Server instance
     * @param sender who to execute the commands as
     * @param input InputStream for commands
     * @param echo true if commands should be echoed back to sender
     * @return true if all commands executed successfully
     * @throws IOException upon I/O error
     */
    public static boolean read(Server server, CommandSender sender, InputStream input, boolean echo) throws IOException {
        List<CommandCall> calls = new ArrayList<CommandCall>();

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
                
                Command command = server.getPluginCommand(c);
                if (command == null) {
                    throw new CommandReaderException(String.format("Unknown command at line %d", lineNo));
                }
                
                calls.add(new CommandCall(command, c, Arrays.copyOfRange(args, 1, args.length)));
            }
        }
        finally {
            in.close();
        }
        
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

        return true;
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
