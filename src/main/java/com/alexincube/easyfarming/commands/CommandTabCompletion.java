package com.alexincube.easyfarming.commands;

import com.alexincube.easyfarming.easyfarming;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CommandTabCompletion implements TabCompleter {
    Plugin plugin = easyfarming.getInstance();
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1){
            List<String> commands = new ArrayList<>();

            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("plugin-required-permission")) {
                if (sender.hasPermission("ef.replanting")) {
                    commands.add("toggle");
                }
            }else{
                commands.add("toggle");
            }

            if (sender.isOp()){
                commands.add("reload");
            }

            return commands;
        }
        return null;
    }
}
