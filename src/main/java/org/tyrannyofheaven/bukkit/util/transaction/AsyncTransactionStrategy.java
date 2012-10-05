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

    public AsyncTransactionStrategy(TransactionStrategy transactionStrategy, Executor executor) {
        transactionExecutor = new TransactionExecutor(transactionStrategy);
        this.executor = executor;
    }

    public Executor getExecutor() {
        return transactionExecutor; // and by executor, we actually mean transactionExecutor
    }

    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("callback cannot be null");
        try {
            // Start collecting runnables
            transactionExecutor.begin();
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
        catch (Error e) {
            throw e;
        }
        catch (RuntimeException e) {
            // No need to wrap these, just re-throw
            throw e;
        }
        catch (Throwable t) {
            throw new TransactionException(t);
        }
    }

}
