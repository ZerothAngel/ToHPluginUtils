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

import static org.tyrannyofheaven.bukkit.util.ToHLoggingUtils.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.CreateSequenceVisitor;
import com.avaje.ebeaninternal.server.ddl.CreateTableVisitor;
import com.avaje.ebeaninternal.server.ddl.DdlGenContext;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.ddl.VisitorUtil;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.io.CharStreams;

public class ToHDatabaseUtils {

    private ToHDatabaseUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    /**
     * Create an EbeanServer instance for a plugin, installing an optional
     * {@link NamingConvention} implementation.
     * 
     * @param plugin the JavaPlugin subclass
     * @param classLoader the plugin's class loader
     * @param namingConvention NamingConvention instance or null
     * @return new EbeanServer instance
     */
    // R.I.P. BUKKIT-3919
    public static EbeanServer createEbeanServer(JavaPlugin plugin, ClassLoader classLoader, NamingConvention namingConvention) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        if (classLoader == null)
            throw new IllegalArgumentException("classLoader cannot be null");

        ServerConfig db = new ServerConfig();

        // All this duplication just for this one line...
        if (namingConvention != null)
            db.setNamingConvention(namingConvention);

        db.setDefaultServer(false);
        db.setRegister(false);
        db.setClasses(plugin.getDatabaseClasses());
        db.setName(plugin.getDescription().getName());
        plugin.getServer().configureDbConfig(db);

        DataSourceConfig ds = db.getDataSourceConfig();

        ds.setUrl(replaceDatabaseString(plugin, ds.getUrl()));
        plugin.getDataFolder().mkdirs();

        ClassLoader previous = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(classLoader);
        EbeanServer ebeanServer = EbeanServerFactory.create(db);
        Thread.currentThread().setContextClassLoader(previous);
        
        return ebeanServer;
    }

    // Copied from JavaPlugin
    private static String replaceDatabaseString(Plugin plugin, String input) {
        input = input.replaceAll("\\{DIR\\}", plugin.getDataFolder().getPath().replaceAll("\\\\", "/") + "/");
        input = input.replaceAll("\\{NAME\\}", plugin.getDescription().getName().replaceAll("[^\\w_-]", ""));
        return input;
    }

    /**
     * Given a Configuration, populate a {@link ToHNamingConvention}.
     * 
     * @param config the Configuration
     * @param namingConvention a ToHNamingConvention instance
     */
    public static void populateNamingConvention(Configuration config, ToHNamingConvention namingConvention) {
        if (config == null)
            throw new IllegalArgumentException("config cannot be null");
        if (namingConvention == null)
            throw new IllegalArgumentException("namingConvention cannot be null");

        namingConvention.clearTableNames();
        ConfigurationSection node = config.getConfigurationSection("tables");
        if (node != null) {
            for (Map.Entry<String, ?> me : node.getValues(false).entrySet()) {
                namingConvention.setTableName(me.getKey(), me.getValue().toString());
            }
        }
    }

    /**
     * Database schema upgrade logic. Maintains a simple schema version table.
     * Generates that or the entire schema as appropriate. Runs schema update
     * scripts from a certain path.
     * 
     * @param ebeanServer the EbeanServer
     * @param namingConvention the associated NamingConvention
     * @param classLoader the plugin's class loader
     * @param pluginEntity any entity class (aside from ToHSchemaVersion) used by the plugin
     * @param updatePath path to the root of the update scripts
     */
    public static void upgradeDatabase(JavaPlugin plugin, NamingConvention namingConvention, ClassLoader classLoader, String updatePath) throws IOException {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        if (namingConvention == null)
            throw new IllegalArgumentException("namingConvention cannot be null");
        if (classLoader == null)
            throw new IllegalArgumentException("classLoader cannot be null");
        if (!ToHStringUtils.hasText(updatePath))
            throw new IllegalArgumentException("updatePath must have a value");

        // Find an entity class that is not ToHSchemaVersion. We'll select the
        // first one that matches from getDatabaseClasses(). This class will be
        // used to determine if the full schema should be generated.
        Class<?> pluginEntity = null;
        for (Class<?> clazz : plugin.getDatabaseClasses()) {
            // Use anything except ToHSchemaVersion
            if (clazz != ToHSchemaVersion.class) {
                pluginEntity = clazz;
                break;
            }
        }
        if (pluginEntity == null)
            throw new IllegalArgumentException("plugin.getDatabaseClasses() must have a non-ToHSchemaVersion class");
        log(plugin, Level.FINE, "Selected %s as plugin-specific entity", pluginEntity.getSimpleName());

        EbeanServer ebeanServer = plugin.getDatabase();
        SpiEbeanServer spiEbeanServer = (SpiEbeanServer)ebeanServer;
        DdlGenerator ddlGenerator = spiEbeanServer.getDdlGenerator();

        // Check schema version
        log(plugin, "Checking database schema...");
        List<ToHSchemaVersion> schemaVersions;
        boolean createSchemaVersionTable;
        try {
            schemaVersions = ebeanServer.find(ToHSchemaVersion.class).orderBy("version").findList();
            createSchemaVersionTable = false;
        }
        catch (PersistenceException e) {
            log(plugin, Level.WARNING, "Schema version table not present");
            schemaVersions = Collections.emptyList();
            createSchemaVersionTable = true;
        }
        
        // Extract highest version
        ToHSchemaVersion schemaVersion = null;
        if (!schemaVersions.isEmpty())
            schemaVersion = schemaVersions.get(schemaVersions.size() - 1);

        // If error, create schema and/or schema version table
        boolean createFullSchema = false;
        if (schemaVersion == null) {
            // (Backwards compatibility)
            log(plugin, "Checking plugin-specific table...");
            try {
                //   Check plugin-specific table
                ebeanServer.find(pluginEntity).findRowCount();
                log(plugin, "Found plugin-specific table");
            }
            catch (PersistenceException e) {
                //   If error, create entire schema
                log(plugin, Level.WARNING, "Plugin-specific table not present");
                createFullSchema = true;
            }

            if (createFullSchema) {
                // Takes precedence over createSchemaVersionTable
                log(plugin, "Creating full plugin schema...");
                ddlGenerator.runScript(false, ddlGenerator.generateCreateDdl());
            }
            else if (createSchemaVersionTable) {
                log(plugin, "Creating schema version table...");
                ddlGenerator.runScript(false, generateSchemaVersionTableDdl(spiEbeanServer, namingConvention));
            }

            //   Insert version 1 into schema version table
            schemaVersion = new ToHSchemaVersion();
            schemaVersion.setVersion(1L);
            saveSchemaVersion(ebeanServer, schemaVersion);
        }
        
        log(plugin, "Current schema version: %s", schemaVersion);

        // Check for update scripts
        DatabasePlatform dbPlatform = spiEbeanServer.getDatabasePlatform();
        String dbUpdatePath = updatePath + "/" + dbPlatform.getName() + "/";
        String commonUpdatePath = updatePath + "/common/";

        // Loop
        for (;;) {
            //   Check for existence of schema+1 update script
            String updateScriptName = String.format("V%d_update.sql", schemaVersion.getVersion() + 1L);
            InputStream is = classLoader.getResourceAsStream(dbUpdatePath + updateScriptName);
            if (is == null)
                is = classLoader.getResourceAsStream(commonUpdatePath + updateScriptName);

            if (is != null) {
                try {
                    // Only execute script if we didn't create full schema
                    if (!createFullSchema) {
                        log(plugin, "Executing schema update script %s", updateScriptName);

                        //   If exists, run it, schema++, insert schema version into schema version table
                        String updateContent = CharStreams.toString(new InputStreamReader(is));
                        updateContent = subsituteTableNames(namingConvention, plugin.getDatabaseClasses(), updateContent);
                        ddlGenerator.runScript(false, updateContent);
                    }
                }
                finally {
                    is.close();
                }

                // Insert new version
                ToHSchemaVersion newSchemaVersion = new ToHSchemaVersion();
                newSchemaVersion.setVersion(schemaVersion.getVersion() + 1L);
                saveSchemaVersion(ebeanServer, newSchemaVersion);

                schemaVersion = newSchemaVersion;
            }
            else {
                // No more versions
                log(plugin, Level.FINE, "Schema update done");
                break;
            }
        }
    }

    // Use Avaje black magic to create only the schema version table
    private static String generateSchemaVersionTableDdl(SpiEbeanServer spiEbeanServer, NamingConvention namingConvention) {
        // Horrible, horrible
        DdlGenContext ctx = new DdlGenContext(spiEbeanServer.getDatabasePlatform(), namingConvention);
        CreateTableVisitor create = new CreateTableVisitor(ctx);
        List<BeanDescriptor<?>> descriptors = new ArrayList<BeanDescriptor<?>>(1);
        descriptors.add(spiEbeanServer.getBeanDescriptor(ToHSchemaVersion.class));
        VisitorUtil.visit(descriptors, create);
        
        // Don't really need this, but full schema gen creates it
        CreateSequenceVisitor createSequence = new CreateSequenceVisitor(ctx);
        VisitorUtil.visit(descriptors, createSequence);

        // ToHSchemaVersion should have no FKs or be referenced anywhere else
//        AddForeignKeysVisitor fkeys = new AddForeignKeysVisitor(ctx);
//        VisitorUtil.visit(descriptors, fkeys);

        ctx.flush();
        return ctx.getContent();
    }

    // Save a new version to the schema table
    private static void saveSchemaVersion(EbeanServer ebeanServer, ToHSchemaVersion schemaVersion) {
        schemaVersion.setTimestamp(new Date());
        ebeanServer.beginTransaction();
        try {
            ebeanServer.save(schemaVersion);
            ebeanServer.commitTransaction();
        }
        finally {
            ebeanServer.endTransaction();
        }
    }

    private static String subsituteTableNames(NamingConvention namingConvention, List<Class<?>> validEntities, String input) {
        String out = input;
        for (Class<?> entityClass : validEntities) {
            if (entityClass == ToHSchemaVersion.class)
                continue; // Updates should never mess with this class
            out = out.replaceAll("\\$\\{" + entityClass.getSimpleName() + "\\}", namingConvention.getTableName(entityClass).getQualifiedName());
        }
        return out;
    }

}
