/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
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

import static org.tyrannyofheaven.bukkit.util.ToHUtils.hasText;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

/**
 * Utilities for checking permissions and displaying error messages.
 * 
 * @author asaddi
 */
public class PermissionUtils {

    private PermissionUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    /**
     * Test if a permissible has multiple permissions.
     * 
     * @param permissible the permissible
     * @param all true if all permissions are required
     * @param permissions the permissions
     * @return true if conditions are met
     */
    public static boolean hasPermissions(Permissible permissible, boolean all, String... permissions) {
        if (permissions == null || permissions.length == 0) return true;
        
        if (all) {
            for (String permission : permissions) {
                if (!hasText(permission))
                    throw new IllegalArgumentException("permission must have a value");

                if (!permissible.hasPermission(permission))
                    return false;
            }
            return true;
        }
        else {
            boolean found = false;
            for (String permission : permissions) {
                if (!hasText(permission))
                    throw new IllegalArgumentException("permission must have a value");

                if (permissible.hasPermission(permission)) {
                    found = true;
                    break;
                }
            }
            return found;
        }
    }

    /**
     * Test if a permissible has at least one permission.
     * 
     * @param permissible the permissible
     * @param permissions the permissions
     * @return true if permissible has at least one permission
     */
    public static boolean hasOnePermission(Permissible permissible, String... permissions) {
        return hasPermissions(permissible, false, permissions);
    }
    
    /**
     * Test if a permissible has all permissions.
     * 
     * @param permissible the permissible
     * @param permissions the permissions
     * @return true if permissible has all permissions
     */
    public static boolean hasAllPermissions(Permissible permissible, String... permissions) {
        return hasPermissions(permissible, true, permissions);
    }

    /**
     * Require a single permission.
     * 
     * @param permissible the permissible to check
     * @param permission the name of the permission
     */
    public static void requirePermission(Permissible permissible, String permission) {
        if (!hasText(permission))
            throw new IllegalArgumentException("permission must have a value");

        if (!permissible.hasPermission(permission)) {
            throw new PermissionException(permission);
        }
    }

    /**
     * Require multiple permissions, all required.
     * 
     * @param permissible the permissible to check
     * @param permissions the names of the permissions
     */
    public static void requireAllPermissions(Permissible permissible, String... permissions) {
        if (!hasAllPermissions(permissible, permissions))
            throw new PermissionException(true, permissions);
    }

    /**
     * Require one of multiple permissions.
     * 
     * @param permissible the permissible to check
     * @param permissions the names of the permissions
     */
    public static void requireOnePermission(Permissible permissible, String... permissions) {
        if (!hasOnePermission(permissible, permissions))
            throw new PermissionException(false, permissions);
    }

    /**
     * Display a helpful error message when a permission check fails.
     * 
     * @param sender the command sender
     * @param permissionException the associated PermissionException
     */
    public static void displayPermissionException(CommandSender sender, PermissionException permissionException) {
        if (sender == null)
            throw new IllegalArgumentException("sender cannot be null");
        if (permissionException == null)
            throw new IllegalArgumentException("permissionException cannot be null");

        if (permissionException.getPermissions().size() == 1) {
            sender.sendMessage(ChatColor.RED + "You need the following permission to do this:");
            sender.sendMessage(ChatColor.DARK_GREEN + "- " + permissionException.getPermissions().get(0));
        }
        else {
            sender.sendMessage(ChatColor.RED + String.format("You need %s of the following permissions to do this:",
                    permissionException.isAll() ? "all" : "one"));
            for (String permission : permissionException.getPermissions()) {
                sender.sendMessage(ChatColor.DARK_GREEN + "- " + permission);
            }
        }
    }

}
