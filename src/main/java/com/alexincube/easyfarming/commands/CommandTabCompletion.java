package com.alexincube.easyfarming.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandTabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1){
            List<String> commands = new ArrayList<>();
            commands.add("toggle");
            commands.add("reload");
            return commands;
        }
        return null;
    }
}
