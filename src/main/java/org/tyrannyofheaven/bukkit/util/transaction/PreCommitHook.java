/*
 * Copyright 2013 ZerothAngel <zerothangel@tyrannyofheaven.org>
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
 * Optional hook to be called before transaction commit.
 * 
 * @author zerothangel
 */
public interface PreCommitHook {

    /**
     * Callback called immediately before transaction commit. If you want to
     * abort, throw an exception.
     * 
     * @param readOnly the read-only flag for this transaction
     */
    public void preCommit(boolean readOnly) throws Exception;

}
