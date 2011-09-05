package org.tyrannyofheaven.bukkit.util.transaction;

import com.avaje.ebean.EbeanServer;

/**
 * TransactionStrategy that executes the action inside an Avaje Ebean
 * transaction. The transaction is committed upon return of the callback.
 * To force rollback, throw an exception.
 * 
 * @author asaddi
 */
public class AvajeTransactionStrategy implements TransactionStrategy {

    private final EbeanServer ebeanServer;

    /**
     * Create an instance associated with the given EbeanServer.
     * 
     * @param ebeanServer the EbeanServer to use for transactions
     */
    public AvajeTransactionStrategy(EbeanServer ebeanServer) {
        if (ebeanServer == null)
            throw new IllegalArgumentException("ebeanServer cannot be null");
        this.ebeanServer = ebeanServer;
    }

    // Retrieve the EbeanServer
    private EbeanServer getEbeanServer() {
        return ebeanServer;
    }

    /* (non-Javadoc)
     * @see org.tyrannyofheaven.bukkit.util.transaction.TransactionStrategy#execute(org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback)
     */
    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("callback cannot be null");
        try {
            getEbeanServer().beginTransaction();
            try {
                T result = callback.doInTransaction();
                getEbeanServer().commitTransaction();
                return result;
            }
            finally {
                getEbeanServer().endTransaction();
            }
        }
        catch (Error e) {
            throw e;
        }
        catch (Throwable t) {
            throw new TransactionException(t);
        }
    }

}
