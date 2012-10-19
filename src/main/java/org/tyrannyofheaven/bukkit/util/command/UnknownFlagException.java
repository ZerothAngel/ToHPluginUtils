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

/**
 * Thrown when an unknown flag is parsed.
 * 
 * @author zerothangel
 */
public class UnknownFlagException extends ParseException {

    private static final long serialVersionUID = -7316597479193239903L;

    private final String flag;

    UnknownFlagException(String flag) {
        super("Unknown flag: " + flag);
        
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

}
