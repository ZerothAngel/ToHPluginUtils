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

/**
 * Back-end-independent interface for performing operations within a transaction.
 * 
 * @author zerothangel
 */
public interface TransactionStrategy {

    /**
     * Execute the given callback within a transaction suitable for this
     * implementation. The transaction is considered read-write.
     * 
     * @param callback the callback
     * @return the result of the callback
     */
    public <T> T execute(TransactionCallback<T> callback);

    /**
     * Execute the given callback within a transaction suitable for this
     * implementation.
     * 
     * @param callback the callback
     * @param readOnly true if this transaction should be considered read-only
     * @return the result of the callback
     */
    public <T> T execute(TransactionCallback<T> callback, boolean readOnly);

}
