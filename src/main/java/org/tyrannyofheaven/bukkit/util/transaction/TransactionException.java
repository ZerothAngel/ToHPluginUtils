package org.tyrannyofheaven.bukkit.util.transaction;

/**
 * Exception thrown when the callback throws an exception.
 * 
 * @author zerothangel
 */
public class TransactionException extends RuntimeException {

    private static final long serialVersionUID = -1958331767457262692L;

    public TransactionException() {
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

}
