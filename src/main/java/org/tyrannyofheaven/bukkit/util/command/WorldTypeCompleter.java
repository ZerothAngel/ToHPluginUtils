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
package org.tyrannyofheaven.bukkit.util.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

/**
 * TypeCompleter for world names.
 * 
 * @author zerothangel
 */
class WorldTypeCompleter implements TypeCompleter {

    @Override
    public List<String> complete(Class<?> clazz, String arg, CommandSender sender, String partial) {
        if (clazz == String.class) {
            List<String> result = new ArrayList<String>();
            for (World world : Bukkit.getWorlds()) {
                if (StringUtil.startsWithIgnoreCase(world.getName(), partial))
                    result.add(world.getName());
            }
            Collections.sort(result);
            return result;
        }
        return Collections.emptyList();
    }

}
