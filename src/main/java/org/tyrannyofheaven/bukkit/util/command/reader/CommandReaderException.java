package org.tyrannyofheaven.bukkit.util.command.reader;

/**
 * Thrown when {@link CommandReader#read(org.bukkit.Server, org.bukkit.command.CommandSender, java.io.InputStream, boolean)}
 * and friends encounter an error.
 * 
 * @author zerothangel
 */
public class CommandReaderException extends RuntimeException {

    private static final long serialVersionUID = 6791041884281671759L;

    public CommandReaderException() {
    }

    public CommandReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandReaderException(String message) {
        super(message);
    }

    public CommandReaderException(Throwable cause) {
        super(cause);
    }

}
