package org.tyrannyofheaven.bukkit.util;

public class ParsedArgs {

    public ParsedArgs(CommandMetaData cmd, String[] args) {
        parse(cmd, args);
    }

    private void parse(CommandMetaData cmd, String[] args) {
        
    }

    public boolean hasOption(String name) {
        // TODO collapse into getValue
        return false;
    }

    public Object getOption(String name) {
        return null;
    }

    public String getValue(String name) {
        return null;
    }

}
