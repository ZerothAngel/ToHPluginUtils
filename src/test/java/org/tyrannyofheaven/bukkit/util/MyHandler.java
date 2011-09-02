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
package org.tyrannyofheaven.bukkit.util;

import org.bukkit.command.CommandSender;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.Option;
import org.tyrannyofheaven.bukkit.util.command.ParseException;
import org.tyrannyofheaven.bukkit.util.command.Require;

public class MyHandler {

    private final FooHandler fooHandler = new FooHandler();

    @Command({"hello", "greetings"})
    public void hello(CommandSender sender, @Option("-f") boolean flag) {
        sender.sendMessage("Hello World!");
        if (flag)
            sender.sendMessage("With flag!");
    }

    @Command("greet")
    public void greet(CommandSender sender, @Option("name") String name, @Option("-o") String opt) {
        sender.sendMessage("Hello, " + name);
        if (opt != null)
            sender.sendMessage("With option = " + opt + "!");
    }

    @Command("say")
    public void say(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < (args.length - 1))
                sb.append(' ');
        }
        sender.sendMessage(sb.toString());
    }

    @Command("foo")
    public FooHandler foo(String[] args) {
        if (args.length == 0)
            throw new ParseException("Missing sub-command");
        return fooHandler;
    }

    // Sub-command handler
    public static class FooHandler {
        
        @Command("hello")
        public void hello(CommandSender sender) {
            sender.sendMessage("Hello from the foo sub-command!");
        }

    }

    @Command("secret")
    @Require("foo.secret")
    public void secret(CommandSender sender, MyPlugin plugin) {
        sender.sendMessage("Spike has a crush on Rarity");
        // Try calling a subclass method
        plugin.test(sender);
    }

}
