package org.tyrannyofheaven.bukkit.util.transaction;

/**
 * Callback interface for performing operations within a transaction.
 * 
 * @author zerothangel
 */
public interface TransactionCallback<T> {

    /**
     * Perform operations within a transaction.
     * @return the result of the operation
     * @throws Exception any thrown exception will result in a rollback
     */
    public T doInTransaction() throws Exception;

}
