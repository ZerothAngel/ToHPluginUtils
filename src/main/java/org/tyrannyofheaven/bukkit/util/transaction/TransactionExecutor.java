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
 * Simple Executor implementation that simply queues up Runnables within a
 * TransactionRunnable.
 * 
 * @author zerothangel
 */
class TransactionExecutor implements Executor {

    private final TransactionStrategy transactionStrategy;

    private final ThreadLocal<TransactionRunnable> currentTransactionRunnableHolder = new ThreadLocal<TransactionRunnable>();

    public TransactionExecutor(TransactionStrategy transactionStrategy) {
        this.transactionStrategy = transactionStrategy;
    }

    @Override
    public void execute(Runnable command) {
        TransactionRunnable currentTransactionRunnable = currentTransactionRunnableHolder.get();
        if (currentTransactionRunnable == null)
            throw new IllegalStateException("No current TransactionRunnable");
        currentTransactionRunnable.addRunnable(command);
    }

    public void begin(boolean readOnly) {
        TransactionRunnable currentTransactionRunnable = currentTransactionRunnableHolder.get();
        if (currentTransactionRunnable != null)
            throw new IllegalStateException("Existing TransactionRunnable found");
        currentTransactionRunnableHolder.set(new TransactionRunnable(transactionStrategy, readOnly));
    }

    public TransactionRunnable end() {
        TransactionRunnable currentTransactionRunnable = currentTransactionRunnableHolder.get();
        if (currentTransactionRunnable == null)
            throw new IllegalStateException("No current TransactionRunnable");
        currentTransactionRunnableHolder.remove();
        return currentTransactionRunnable;
    }

}
