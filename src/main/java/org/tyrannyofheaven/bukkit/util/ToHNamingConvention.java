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
package org.tyrannyofheaven.bukkit.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import org.tyrannyofheaven.bukkit.util.ToHStringUtils;

import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.TableName;
import com.avaje.ebean.config.UnderscoreNamingConvention;

/**
 * Avaje {@link NamingConvention} implementation that accepts custom names for
 * mapped beans. Otherwise behaves the same as {@link UnderscoreNamingConvention},
 * the default used by Avaje.
 * 
 * @author zerothangel
 */
public class ToHNamingConvention extends UnderscoreNamingConvention {

    private final Map<String, String> tableNames = new HashMap<String, String>();

    /**
     * Construct a new instance and configure it so it only accepts table name
     * mappings of the classes specified by {@link JavaPlugin#getDatabaseClasses()}.
     * 
     * @param plugin the JavaPlugin subclass
     */
    public ToHNamingConvention(JavaPlugin plugin) {
        // Set up null placeholders
        for (Class<?> clazz : plugin.getDatabaseClasses()) {
            tableNames.put(clazz.getSimpleName(), null);
        }
    }
    
    /**
     * Clear all table name mappings.
     */
    public void clearTableNames() {
        for (Map.Entry<String, String> me : tableNames.entrySet()) {
            me.setValue(null);
        }
    }

    /**
     * Add a table name mapping for the given class.
     * 
     * @param className the simple name of the class
     * @param tableName the table name. May be qualified with catalog/schema. May be null.
     */
    public void setTableName(String className, String tableName) {
        if (!ToHStringUtils.hasText(tableName))
            tableName = null; // Normalize
        if (tableNames.containsKey(className)) {
            tableNames.put(className, tableName);
        }
    }

    /* (non-Javadoc)
     * @see com.avaje.ebean.config.AbstractNamingConvention#getTableName(java.lang.Class)
     */
    @Override
    public TableName getTableName(Class<?> beanClass) {
        String qualifiedTableName = tableNames.get(beanClass.getSimpleName());
        if (qualifiedTableName != null) {
            return new TableName(qualifiedTableName);
        }
        return super.getTableName(beanClass);
    }

}
