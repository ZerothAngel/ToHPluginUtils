package org.tyrannyofheaven.bukkit.util.transaction;

/**
 * TransactionStrategy that does nothing special.
 * 
 * @author asaddi
 */
public class NullTransactionStrategy implements TransactionStrategy {

    /* (non-Javadoc)
     * @see org.tyrannyofheaven.bukkit.util.transaction.TransactionStrategy#execute(org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback)
     */
    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("callback cannot be null");
        try {
            return callback.doInTransaction();
        }
        catch (Error e) {
            throw e;
        }
        catch (Throwable t) {
            throw new TransactionException(t);
        }
    }

}
