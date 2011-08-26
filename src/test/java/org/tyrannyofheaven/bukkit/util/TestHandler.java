package org.tyrannyofheaven.bukkit.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.tyrannyofheaven.bukkit.util.Command;
import org.tyrannyofheaven.bukkit.util.Option;

public class TestHandler {

    @Command({"hello", "greetings"})
    public void hello(Player player, @Option("-f") boolean flag) {
        player.sendMessage(ChatColor.GREEN + "Hello World!");
        if (flag)
            player.sendMessage(ChatColor.YELLOW + "With flag!");
    }

}
