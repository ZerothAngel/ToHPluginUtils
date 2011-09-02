package org.tyrannyofheaven.bukkit.util;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    protected void test(CommandSender sender) {
        sender.sendMessage("yay!");
    }

}
