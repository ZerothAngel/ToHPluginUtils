/*
 * Copyright 2011 ZerothAngel <zerothangel@tyrannyofheaven.org>
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

import com.avaje.ebean.EbeanServer;

/**
 * TransactionStrategy that executes the action inside an Avaje Ebean
 * transaction. The transaction is committed upon return of the callback.
 * To force rollback, throw an exception.
 * 
 * @author zerothangel
 */
public class AvajeTransactionStrategy implements TransactionStrategy {

    private final EbeanServer ebeanServer;

    private final PreBeginHook preBeginHook;

    private final PreCommitHook preCommitHook;

    /**
     * Create an instance associated with the given EbeanServer.
     * 
     * @param ebeanServer the EbeanServer to use for transactions
     * @param preCommitHook the pre-commit hook or null
     */
    public AvajeTransactionStrategy(EbeanServer ebeanServer, PreBeginHook preBeginHook, PreCommitHook preCommitHook) {
        if (ebeanServer == null)
            throw new IllegalArgumentException("ebeanServer cannot be null");
        this.ebeanServer = ebeanServer;
        this.preBeginHook = preBeginHook;
        this.preCommitHook = preCommitHook;
    }

    /**
     * Create an instance associated with the given EbeanServer.
     * 
     * @param ebeanServer the EbeanServer to use for transactions
     */
    public AvajeTransactionStrategy(EbeanServer ebeanServer) {
        this(ebeanServer, null, null);
    }

    // Retrieve the EbeanServer
    private EbeanServer getEbeanServer() {
        return ebeanServer;
    }

    // Retrieve the pre-begin hook
    public PreBeginHook getPreBeginHook() {
        return preBeginHook;
    }

    // Retrieve the pre-commit hook
    private PreCommitHook getPreCommitHook() {
        return preCommitHook;
    }

    /* (non-Javadoc)
     * @see org.tyrannyofheaven.bukkit.util.transaction.TransactionStrategy#execute(org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback)
     */
    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        return execute(callback, false);
    }

    /* (non-Javadoc)
     * @see org.tyrannyofheaven.bukkit.util.transaction.TransactionStrategy#execute(org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback, boolean)
     */
    @Override
    public <T> T execute(TransactionCallback<T> callback, boolean readOnly) {
        if (callback == null)
            throw new IllegalArgumentException("callback cannot be null");
        try {
            if (getPreBeginHook() != null)
                getPreBeginHook().preBegin(readOnly);
            getEbeanServer().beginTransaction();
            try {
                T result = callback.doInTransaction();
                if (getPreCommitHook() != null)
                    getPreCommitHook().preCommit(readOnly);
                getEbeanServer().commitTransaction();
                return result;
            }
            finally {
                getEbeanServer().endTransaction();
            }
        }
        catch (Error | RuntimeException e) {
            // No need to wrap these, just re-throw
            throw e;
        }
        catch (Throwable t) {
            throw new TransactionException(t);
        }
    }

}
