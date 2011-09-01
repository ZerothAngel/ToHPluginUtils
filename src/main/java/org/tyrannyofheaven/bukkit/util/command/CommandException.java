package org.tyrannyofheaven.bukkit.util.command;

/**
 * Exception thrown when there is an error (usually a programming error) in a
 * command handler.
 * 
 * @author asaddi
 */
public class CommandException extends RuntimeException {

    private static final long serialVersionUID = -4453503398619490556L;

    public CommandException() {
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
