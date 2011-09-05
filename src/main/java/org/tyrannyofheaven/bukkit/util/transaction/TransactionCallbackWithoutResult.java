package org.tyrannyofheaven.bukkit.util.transaction;

/**
 * Convenience wrapper for callbacks that don't return a result.
 * 
 * @author asaddi
 */
public abstract class TransactionCallbackWithoutResult implements TransactionCallback<Object> {

    /* (non-Javadoc)
     * @see org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback#doInTransaction()
     */
    @Override
    public final Object doInTransaction() throws Exception {
        doInTransactionWithoutResult();
        return null;
    }

    /**
     * Perform operations within a transaction.
     * 
     * @throws Exception any thrown exception will result in a rollback
     */
    public abstract void doInTransactionWithoutResult() throws Exception;

}
