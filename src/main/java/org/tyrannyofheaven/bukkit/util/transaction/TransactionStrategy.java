package org.tyrannyofheaven.bukkit.util.transaction;

/**
 * Back-end-independent interface for performing operations within a transaction.
 * 
 * @author asaddi
 */
public interface TransactionStrategy {

    /**
     * Execute the given callback within a transaction suitable for this
     * implementation.
     * 
     * @param callback the callback
     * @return the result of the callback
     */
    public <T> T execute(TransactionCallback<T> callback);

}
