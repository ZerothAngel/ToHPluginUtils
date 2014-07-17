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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CascadingUuidResolver implements UuidResolver {

    private final List<UuidResolver> uuidResolvers;

    public CascadingUuidResolver(UuidResolver... uuidResolvers) {
        this(Arrays.asList(uuidResolvers));
    }

    public CascadingUuidResolver(List<UuidResolver> uuidResolvers) {
        this.uuidResolvers = new ArrayList<>(uuidResolvers);
    }

    @Override
    public UuidDisplayName resolve(String username) {
        for (UuidResolver uuidResolver : uuidResolvers) {
            UuidDisplayName udn = uuidResolver.resolve(username);
            if (udn != null) return udn;
        }
        return null;
    }

    @Override
    public UuidDisplayName resolve(String username, boolean cacheOnly) {
        for (UuidResolver uuidResolver : uuidResolvers) {
            UuidDisplayName udn = uuidResolver.resolve(username, cacheOnly);
            if (udn != null) return udn;
        }
        return null;
    }

    @Override
    public Map<String, UuidDisplayName> resolve(Collection<String> usernames) throws Exception {
        // Remaining set of usernames to resolve
        Set<String> remaining = new HashSet<>();
        for (String username : usernames) {
            // Ensure everything is lowercased
            remaining.add(username.toLowerCase());
        }

        Map<String, UuidDisplayName> result = new LinkedHashMap<>();
        for (UuidResolver uuidResolver : uuidResolvers) {
            if (remaining.isEmpty()) break;
            Map<String, UuidDisplayName> resolved = uuidResolver.resolve(remaining);
            // Merge results, but don't overwrite existing entries
            for (Map.Entry<String, UuidDisplayName> me : resolved.entrySet()) {
                String username = me.getKey();
                if (!result.containsKey(username)) result.put(username, me.getValue());
            }
            // Adjust remaining set of usernames
            remaining.removeAll(resolved.keySet());
        }
        return result;
    }

    @Override
    public void preload(String username, UUID uuid) {
        for (UuidResolver uuidResolver : uuidResolvers) {
            uuidResolver.preload(username, uuid);
        }
    }

}
