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

import static org.tyrannyofheaven.bukkit.util.ToHStringUtils.hasText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to parse a command's arguments and store the results.
 * 
 * @author zerothangel
 */
final class ParsedArgs {

    private final Map<String, String> options = new HashMap<String, String>();

    private String[] rest = new String[0];

    private OptionMetaData unparsedArgument = null;

    private boolean parsedPositional = false;

    private boolean parsed = false;

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
    public void parse(CommandMetaData cmd, String[] args) {
        if (cmd == null)
            throw new IllegalArgumentException("cmd cannot be null");

        if (args == null)
            args = new String[0];

        if (parsed)
            throw new IllegalStateException("parse already called");
        parsed = true;

        int pos = 0;
        
        // Parse flags
        while (pos < args.length) {
            String arg = args[pos];
            if ("--".equals(arg)) {
                // explicit end of flags
                pos++;
                parsedPositional = true; // no going back
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
                    throw new UnknownFlagException(arg);
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
                        throw new MissingValueException(omd, arg);
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
                    if (omd.isNullable()) {
                        // NB: No exception will be thrown and rest of arguments
                        // will be unset. Use with care.
                        unparsedArgument = omd;
                        break;
                    }
                    else {
                        // Ran out of args
                        throw new MissingValueException(omd);
                    }
                }
                else {
                    options.put(omd.getName(), args[pos++]);
                    parsedPositional = true;
                }
            }
            else {
                if (pos >= args.length) {
                    // No more args, this and the rest should be optional
                    unparsedArgument = omd;
                    break;
                }
                else {
                    options.put(omd.getName(), args[pos++]);
                    parsedPositional = true;
                }
            }
        }

        rest = Arrays.copyOfRange(args, pos, args.length);
    }

    /**
     * Retrieve associated value for an option.
     * 
     * @param name the option name
     * @return the associated String value
     */
    public String getOption(String name) {
        if (!hasText(name))
            throw new IllegalArgumentException("name must have a value");

        return options.get(name);
    }

    /**
     * Retrieve option map.
     * 
     * @return the option map
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Retrieve unparsed positional parameters.
     * 
     * @return unparsed positional parameters
     */
    public String[] getRest() {
        return rest;
    }

    /**
     * Retrieve OptionMetaData of first unparsed optional positional parameter.
     * 
     * @return OptionMetaData of first unparsed optional positional parameter, or null
     */
    public OptionMetaData getUnparsedArgument() {
        return unparsedArgument;
    }

    /**
     * Returns whether or not any positional arguments have been parsed yet.
     * 
     * @return true if positional arguments have been parsed
     */
    public boolean isParsedPositional() {
        return parsedPositional;
    }

}
