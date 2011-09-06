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
 * TransactionStrategy that does nothing special.
 * 
 * @author asaddi
 */
public class NullTransactionStrategy implements TransactionStrategy {

    /* (non-Javadoc)
     * @see org.tyrannyofheaven.bukkit.util.transaction.TransactionStrategy#execute(org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback)
     */
    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        if (callback == null)
            throw new IllegalArgumentException("callback cannot be null");
        try {
            return callback.doInTransaction();
        }
        catch (Error e) {
            throw e;
        }
        catch (Throwable t) {
            throw new TransactionException(t);
        }
    }

}
