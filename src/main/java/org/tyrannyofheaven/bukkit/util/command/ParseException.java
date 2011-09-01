package org.tyrannyofheaven.bukkit.util.command;

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
