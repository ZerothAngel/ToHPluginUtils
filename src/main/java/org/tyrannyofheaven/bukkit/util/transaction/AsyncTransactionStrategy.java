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

import java.util.concurrent.Executor;

/**
 * Asynchronous TransactionStrategy implementation that collects write operations
 * and then executes them (within the given TransactionStrategy) using the given
 * Executor.
 * 
 * @author zerothangel
 */
public class AsyncTransactionStrategy implements TransactionStrategy {

    private final TransactionExecutor transactionExecutor;

    private final Executor executor;

    private final PreBeginHook preBeginHook;

    public AsyncTransactionStrategy(TransactionStrategy transactionStrategy, Executor executor, PreBeginHook preBeginHook) {
        transactionExecutor = new TransactionExecutor(transactionStrategy);
        this.executor = executor;
        this.preBeginHook = preBeginHook;
    }

    public AsyncTransactionStrategy(TransactionStrategy transactionStrategy, Executor executor) {
        this(transactionStrategy, executor, null);
    }

    public Executor getExecutor() {
        return transactionExecutor; // and by executor, we actually mean transactionExecutor
    }

    // Retrieve pre-begin hook
    private PreBeginHook getPreBeginHook() {
        return preBeginHook;
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
            // Start collecting runnables
            if (getPreBeginHook() != null)
                getPreBeginHook().preBegin(readOnly);
            transactionExecutor.begin(readOnly);
            boolean success = false; // so we know we executed callback successfully
            try {
                T result = callback.doInTransaction();
                success = true;
                return result;
            }
            finally {
                TransactionRunnable transactionRunnable = transactionExecutor.end();
                if (!transactionRunnable.isEmpty() && success) {
                    // Got something, execute it async
                    executor.execute(transactionRunnable);
                }
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
