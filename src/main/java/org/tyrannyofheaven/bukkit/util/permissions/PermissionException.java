package org.tyrannyofheaven.bukkit.util.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PermissionException extends RuntimeException {

    private static final long serialVersionUID = 7965054516381099359L;

    private final List<String> permissions;
    
    private final boolean all;
    
    public PermissionException(String permission) {
        this.permissions = Collections.singletonList(permission);
        this.all = true;
    }
    
    public PermissionException(boolean all, String... permissions) {
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(permissions)));
        this.all = all;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public boolean isAll() {
        return all;
    }

}
