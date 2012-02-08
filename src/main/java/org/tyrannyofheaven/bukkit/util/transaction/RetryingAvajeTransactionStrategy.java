/*
 * Copyright 2012 ZerothAngel <zerothangel@tyrannyofheaven.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tyrannyofheaven.bukkit.util.transaction;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;

/**
 * TransactionStrategy that executes the action inside an Avaje Ebean
 * transaction. The transaction is committed upon return of the callback.
 * To force rollback, throw an exception.
 * 
 * Any PersistenceExceptions caught will cause the transaction to be
 * retried (up to maxRetries times).
 * 
 * @author zerothangel
 */
public class RetryingAvajeTransactionStrategy implements TransactionStrategy {

    private final EbeanServer ebeanServer;

    private final int maxRetries;

    /**
     * Create an instance associated with the given EbeanServer.
     * 
     * @param ebeanServer the EbeanServer to use for transactions
     */
    public RetryingAvajeTransactionStrategy(EbeanServer ebeanServer, int maxRetries) {
        if (ebeanServer == null)
            throw new IllegalArgumentException("ebeanServer cannot be null");
        if (maxRetries < 1)
            throw new IllegalArgumentException("maxRetries must be > 0");
        this.ebeanServer = ebeanServer;
        this.maxRetries = maxRetries;
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
        PersistenceException savedPE = null;
        for (int attempt = -1; attempt < maxRetries; attempt++) {
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
            catch (PersistenceException e) {
                savedPE = e;
                continue;
            }
            catch (RuntimeException e) {
                // No need to wrap these, just re-throw
                throw e;
            }
            catch (Throwable t) {
                throw new TransactionException(t);
            }
        }

        // At this point, we've run out of attempts
        throw savedPE;
    }

}
