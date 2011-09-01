package org.tyrannyofheaven.bukkit.util.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ParsedArgs {

    private final Map<String, String> options;

    private final String[] rest;

    private ParsedArgs(Map<String, String> options, String[] rest) {
        if (options == null)
            throw new IllegalArgumentException("options cannot be null");

        if (rest == null)
            rest = new String[0];

        this.options = options;
        this.rest = rest;
    }

    private static OptionMetaData getOption(String flag, Set<OptionMetaData> options) {
        for (OptionMetaData omd : options) {
            for (String name : omd.getNames()) {
                if (flag.equals(name)) {
                    return omd;
                }
            }
        }
        return null;
    }

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
            if (OptionMetaData.isArgument(arg)) {
                // positional argument
                break;
            }
            else {
                OptionMetaData omd = getOption(arg, cmd.getFlagOptions());
                if (omd == null) {
                    // Unknown option
                    return null;
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
                        return null;
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
                    return null;
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

    public boolean hasOption(String name) {
        if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name must have a value");

        // TODO collapse into getValue
        return options.containsKey(name);
    }

    public String getOption(String name) {
        if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name must have a value");

        return options.get(name);
    }

    public String[] getRest() {
        return Arrays.copyOf(rest, rest.length);
    }

}
