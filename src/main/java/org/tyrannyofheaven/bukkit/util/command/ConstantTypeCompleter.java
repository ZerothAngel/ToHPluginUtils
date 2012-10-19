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

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

/**
 * Simple TypeCompleter that simply returns the arguments (whitespace-separated).
 * 
 * @author zerothangel
 */
class ConstantTypeCompleter implements TypeCompleter {

    @Override
    public List<String> complete(Class<?> clazz, String arg, CommandSender sender, String partial) {
        if (arg != null) {
            List<String> result = new ArrayList<String>();
            String[] parts = arg.split("\\s+");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty() && StringUtil.startsWithIgnoreCase(part, partial))
                    result.add(part);
            }
            return result;
        }
        return Collections.emptyList();
    }

}
