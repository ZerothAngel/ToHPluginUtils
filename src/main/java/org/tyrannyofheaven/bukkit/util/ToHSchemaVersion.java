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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Avaje entity class for use with {@link ToHDatabaseUtils#upgradeDatabase(org.bukkit.plugin.java.JavaPlugin, com.avaje.ebean.config.NamingConvention, ClassLoader, String)}.
 * Requires that the plugin also use {@link ToHNamingConvention} when creating the EbeanServer.
 * This ensures this entity has a plugin-specific name.
 * 
 * @author zerothangel
 */
@Entity
public class ToHSchemaVersion {

    private long version;
    
    private Date timestamp;

    @Id
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Column(nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ToHSchemaVersion)) return false;
        ToHSchemaVersion o = (ToHSchemaVersion)obj;
        return getVersion() == o.getVersion();
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getVersion()).hashCode();
    }

    @Override
    public String toString() {
        return String.format("%d (%s)", getVersion(), getTimestamp());
    }

}
