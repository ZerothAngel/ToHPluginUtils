/*
 * Copyright 2014 ZerothAngel <zerothangel@tyrannyofheaven.org>
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
package org.tyrannyofheaven.bukkit.util.uuid;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface UuidResolver {

    /**
     * Looks up the UUID of a username. Possibly blocking.
     * 
     * @param username the username
     * @return a {@link UuidDisplayName} instance. null if lookup failed. Note that both
     *     properties (uuid and displayName) must be returned. If no display name
     *     lookup is possible, simply returning the passed-in username is acceptable.
     */
    public UuidDisplayName resolve(String username);

    /**
     * Looks up the UUID of a username. Possibly blocking.
     * 
     * @param username the username
     * @param cacheOnly if true, then only the cache (if this implementation supports one) will
     *     be consulted. This method must not block in that case. If false then behaves exactly
     *     like {@link #resolve(String)}.
     * @return a {@link UuidDisplayName} instance. null if lookup failed. Note that both
     *     properties (uuid and displayName) must be returned. If no display name
     *     lookup is possible, simply returning the passed-in username is acceptable.
     */
    public UuidDisplayName resolve(String username, boolean cacheOnly);

    /**
     * Bulk resolution of usernames to UUIDs. Possibly blocking.
     * 
     * @param usernames collection of usernames to resolve
     * @return map of lowercased username to corresponding {@link UuidDisplayName}. Like other
     *     methods in this interface, both properties must be filled.
     * @throws IOException if something went wrong resolving the usernames
     */
    public Map<String, UuidDisplayName> resolve(Collection<String> usernames) throws Exception;

    /**
     * Pre-load the cache (if this implementation has one) with the given
     * username-to-UUID mapping. Note: the underlying implementation is under
     * no obligation to honor this mapping; this is just a hint.
     * 
     * @param username the username
     * @param uuid the associated UUID
     */
    public void preload(String username, UUID uuid);

    /**
     * Hint to the underlying implementation that the cache entry for the
     * given username should be invalidated.
     * 
     * @param username the username
     */
    public void invalidate(String username);

    /**
     * Hint to the underlying implementation to invalidate all cache entries.
     */
    public void invalidateAll();

}
