/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
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
