/*
 * Copyright 2011 ZerothAngel <zerothangel@tyrannyofheaven.org>
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
package org.tyrannyofheaven.bukkit.util.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when a permissible does not have the required permission.
 * 
 * @author zerothangel
 */
public class PermissionException extends RuntimeException {

    private static final long serialVersionUID = 4064063141792654633L;

    private final List<String> permissions;
    
    private final boolean all;

    private final boolean checkNegations;

    /**
     * Create a PermissionException for a single permission.
     * 
     * @param permission name of the permission
     */
    public PermissionException(String permission) {
        this.permissions = Collections.singletonList(permission);
        this.all = true;
        this.checkNegations = false;
    }

    /**
     * Create a PermissionException for multiple permissions.
     *  
     * @param all true if all permissions are required
     * @param permissions name of permissions
     */
    public PermissionException(boolean all, boolean checkNegations, String... permissions) {
        this.permissions = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(permissions)));
        this.all = all;
        this.checkNegations = checkNegations;
    }

    /**
     * Retrieve the associated permissions.
     * 
     * @return the permissions
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Retrieve whether or not all permissions are required.
     * 
     * @return true if all permissions are required
     */
    public boolean isAll() {
        return all;
    }

    /**
     * Retrieve whether or not negations were checked.
     * 
     * @return true if negations were checked
     */
    public boolean isCheckNegations() {
        return checkNegations;
    }

}
