package org.tyrannyofheaven.bukkit.util.command;

/**
 * Thrown when something is wrong with the command arguments. Should result in
 * an error message being shown to the user.
 * 
 * @author zerothangel
 */
public class ParseException extends CommandException {

    private static final long serialVersionUID = 6379096495842869497L;

    public ParseException() {
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

}
