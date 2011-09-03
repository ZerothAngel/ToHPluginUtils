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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

/**
 * Convenience class to parse a command's arguments and store the results.
 * 
 * @author asaddi
 */
final class ParsedArgs {

    private final Map<String, String> options;

    private final String[] rest;

    // Create an instance with the given parsed results
    private ParsedArgs(Map<String, String> options, String[] rest) {
        if (options == null)
            throw new IllegalArgumentException("options cannot be null");

        if (rest == null)
            rest = new String[0];

        this.options = options;
        this.rest = rest;
    }

    // Return the OptionMetaData with the given name (usually a flag)
    private static OptionMetaData getOption(String flag, List<OptionMetaData> options) {
        for (OptionMetaData omd : options) {
            for (String name : omd.getNames()) {
                if (flag.equals(name)) {
                    return omd;
                }
            }
        }
        return null;
    }

    /**
     * Parse command arguments according to the given CommandMetaData.
     * 
     * @param cmd
     * @param args
     * @return 
     */
    public static ParsedArgs parse(CommandMetaData cmd, String[] args) {
        if (cmd == null)
            throw new IllegalArgumentException("cmd cannot be null");

        if (args == null)
            args = new String[0];

        Map<String, String> options = new HashMap<String, String>();

        int pos = 0;
        
        // Parse flags
        while (pos < args.length) {
            String arg = args[pos];
            if ("--".equals(arg)) {
                // explicit end of flags
                pos++;
                break;
            }
            else if (OptionMetaData.isArgument(arg) || cmd.getFlagOptions().isEmpty()) {
                // positional argument
                break;
            }
            else {
                OptionMetaData omd = getOption(arg, cmd.getFlagOptions());
                if (omd == null) {
                    // Unknown option
                    throw new ParseException(ChatColor.RED + "Unknown flag: " + arg);
                }
                // Special handling of Boolean and boolean
                if (omd.getType() == Boolean.class || omd.getType() == Boolean.TYPE) {
                    options.put(omd.getName(), ""); // value doesn't matter, only existence
                }
                else {
                    // Get value
                    pos++;
                    if (pos >= args.length) {
                        // Premature end
                        throw new ParseException(ChatColor.RED + "Missing value for flag: " + arg);
                    }
                    
                    options.put(omd.getName(), args[pos]);
                }
                pos++;
            }
        }
        
        // Parse positional args
        for (OptionMetaData omd : cmd.getPositionalArguments()) {
            if (!omd.isOptional()) {
                if (pos >= args.length) {
                    // Ran out of args
                    throw new ParseException(ChatColor.RED + "Missing argument: " + omd.getName());
                }
                else {
                    options.put(omd.getName(), args[pos++]);
                }
            }
            else {
                if (pos >= args.length) {
                    // No more args, this and the rest should be optional
                    break;
                }
                else {
                    options.put(omd.getName(), args[pos++]);
                }
            }
        }

        return new ParsedArgs(options, Arrays.copyOfRange(args, pos, args.length));
    }

    /**
     * Retrieve associated value for an option.
     * 
     * @param name the option name
     * @return the associated String value
     */
    public String getOption(String name) {
        if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name must have a value");

        return options.get(name);
    }

    /**
     * Retrieve unparsed positional parameters.
     * 
     * @return
     */
    public String[] getRest() {
        return Arrays.copyOf(rest, rest.length);
    }

}
