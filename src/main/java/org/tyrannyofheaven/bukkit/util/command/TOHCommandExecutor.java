package org.tyrannyofheaven.bukkit.util.command;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionException;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils;

public class TOHCommandExecutor implements CommandExecutor {

    private final Plugin plugin;

    private final HandlerExecutor rootHandlerExecutor;
    
    public TOHCommandExecutor(Plugin plugin, Object... handlers) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");

        this.plugin = plugin;

        rootHandlerExecutor = new HandlerExecutor(plugin, handlers);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return rootHandlerExecutor.execute(sender, command.getName(), args);
        }
        catch (PermissionException e) {
            PermissionUtils.displayPermissionException(sender, e);
            return true;
        }
        catch (ParseException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            return false;
        }
        catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Plugin error; see server log");
            plugin.getServer().getLogger().log(Level.SEVERE, String.format("[%s] Unhandled exception:", plugin.getDescription().getName()), e);
            return true;
        }
    }

}
