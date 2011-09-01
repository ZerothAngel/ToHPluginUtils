package org.tyrannyofheaven.bukkit.util.permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

public class PermissionUtils {

    private PermissionUtils() {
        // Don't instantiate me!
        throw new AssertionError();
    }

    public static void requirePermission(Permissible player, String permission) {
        if (!player.hasPermission(permission)) {
            throw new PermissionException(permission);
        }
    }

    public static void requireAllPermissions(Permissible player, String... permissions) {
        if (permissions == null || permissions.length == 0) return;

        for (String permission : permissions) {
            if (!player.hasPermission(permission)) {
                throw new PermissionException(true, permissions);
            }
        }
    }

    public static void requireOnePermission(Permissible player, String... permissions) {
        if (permissions == null || permissions.length == 0) return;

        boolean found = false;
        for (String permission : permissions) {
            if (player.hasPermission(permission)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new PermissionException(false, permissions);
        }
    }

    public static void displayPermissionException(CommandSender sender, PermissionException e) {
        if (e.getPermissions().size() == 1) {
            sender.sendMessage(ChatColor.RED + "You need the following permission to do this:");
            sender.sendMessage(ChatColor.GREEN + "- " + e.getPermissions().get(0));
        }
        else {
            sender.sendMessage(ChatColor.RED + String.format("You need %s of the following permissions to do this:",
                    e.isAll() ? "all" : "one"));
            for (String permission : e.getPermissions()) {
                sender.sendMessage(ChatColor.GREEN + "- " + permission);
            }
        }
    }

}
