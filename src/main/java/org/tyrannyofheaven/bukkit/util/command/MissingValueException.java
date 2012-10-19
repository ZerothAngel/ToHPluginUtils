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
 * Mainly used for tab completion support. Contains state of partial parse.
 * 
 * @author zerothangel
 */
public class MissingValueException extends ParseException {

    private static final long serialVersionUID = 9207028064109673003L;

    private final OptionMetaData optionMetaData;

    MissingValueException(OptionMetaData optionMetaData) {
        super("Missing argument: " + optionMetaData.getName());
        this.optionMetaData = optionMetaData;
    }

    MissingValueException(OptionMetaData optionMetaData, String flag) {
        super("Missing value for flag: " + flag); // could use optionMetaData.getName(), but want passed-in name
        this.optionMetaData = optionMetaData;
    }

    public OptionMetaData getOptionMetaData() {
        return optionMetaData;
    }

}
