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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback;
import org.tyrannyofheaven.bukkit.util.transaction.TransactionStrategy;

/**
 * A Runnable that holds a sequence of writing Runnables to be executed
 * transactionally.
 * 
 * @author zerothangel
 */
class TransactionRunnable implements Runnable, TransactionCallback<Object> {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final TransactionStrategy transactionStrategy;

    private final boolean readOnly;

    private final List<Runnable> runnables = new ArrayList<Runnable>();

    public TransactionRunnable(TransactionStrategy transactionStrategy, boolean readOnly) {
        this.transactionStrategy = transactionStrategy;
        this.readOnly = readOnly;
    }

    public void addRunnable(Runnable runnable) {
        runnables.add(runnable);
    }

    public boolean isEmpty() {
        return runnables.isEmpty();
    }

    private TransactionStrategy getTransactionStrategy() {
        return transactionStrategy;
    }

    private List<Runnable> getRunnables() {
        return runnables;
    }

    @Override
    public void run() {
        try {
            getTransactionStrategy().execute(this, readOnly);
        }
        catch (Error e) {
            throw e;
        }
        catch (Throwable t) {
            logger.log(Level.SEVERE, "Error executing transaction", t);
        }
    }

    @Override
    public Object doInTransaction() throws Exception {
        for (Runnable runnable : getRunnables()) {
            runnable.run();
        }
        return null;
    }

}
