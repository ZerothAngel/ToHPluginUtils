package org.tyrannyofheaven.bukkit.util.permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

/**
 * Utilities for checking permissions and displaying error messages.
 * 
 * @author zerothangel
 */
public class PermissionUtils {

    private PermissionUtils() {
        // Don't instantiate me!
        throw new AssertionError();
    }

    /**
     * Require a single permission.
     * 
     * @param permissible the permissible to check
     * @param permission the name of the permission
     */
    public static void requirePermission(Permissible permissible, String permission) {
        if (permission == null || permission.trim().length() == 0)
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
        if (permissions == null || permissions.length == 0) return;

        for (String permission : permissions) {
            if (permission == null || permission.trim().length() == 0)
                throw new IllegalArgumentException("permission must have a value");

            if (!permissible.hasPermission(permission)) {
                throw new PermissionException(true, permissions);
            }
        }
    }

    /**
     * Require one of multiple permissions.
     * 
     * @param permissible the permissible to check
     * @param permissions the names of the permissions
     */
    public static void requireOnePermission(Permissible permissible, String... permissions) {
        if (permissions == null || permissions.length == 0) return;

        boolean found = false;
        for (String permission : permissions) {
            if (permission == null || permission.trim().length() == 0)
                throw new IllegalArgumentException("permission must have a value");

            if (permissible.hasPermission(permission)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new PermissionException(false, permissions);
        }
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
            sender.sendMessage(ChatColor.GREEN + "- " + permissionException.getPermissions().get(0));
        }
        else {
            sender.sendMessage(ChatColor.RED + String.format("You need %s of the following permissions to do this:",
                    permissionException.isAll() ? "all" : "one"));
            for (String permission : permissionException.getPermissions()) {
                sender.sendMessage(ChatColor.GREEN + "- " + permission);
            }
        }
    }

}
