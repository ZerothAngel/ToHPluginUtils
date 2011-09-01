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

    private static final long serialVersionUID = 7965054516381099359L;

    private final List<String> permissions;
    
    private final boolean all;

    /**
     * Create a PermissionException for a single permission.
     * 
     * @param permission name of the permission
     */
    public PermissionException(String permission) {
        this.permissions = Collections.singletonList(permission);
        this.all = true;
    }

    /**
     * Create a PermissionException for multiple permissions.
     *  
     * @param all true if all permissions are required
     * @param permissions name of permissions
     */
    public PermissionException(boolean all, String... permissions) {
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(permissions)));
        this.all = all;
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

}
