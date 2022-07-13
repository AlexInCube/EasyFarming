package com.alexincube.easyfarming.commands;

import com.alexincube.easyfarming.easyfarming;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class CommandHandler implements CommandExecutor{
    Plugin plugin = easyfarming.getInstance();
    FileConfiguration config = plugin.getConfig();
    String prefix = config.getString("messages.prefix");
    private String translateHexColorCodes(String message)
    {
        final Pattern hexPattern = Pattern.compile("#" +"([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    public void sendPluginMessage (String configMessage, Player player){
        player.sendMessage(translateHexColorCodes(prefix + config.getString(configMessage)));
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return true;

        switch (args[0]) {
            case ("toggle") -> {
                if (!(sender instanceof Player p)) return true;

                if (config.getBoolean("plugin-required-permission")){
                    if (!sender.hasPermission("ef.replanting")){
                        sendPluginMessage("messages.no-permission",p);
                        return true;
                    }
                }

                PersistentDataContainer data = p.getPersistentDataContainer();

                NamespacedKey enableField = new NamespacedKey(plugin, "ef_enable");

                if (data.has(enableField, PersistentDataType.BYTE)) {
                    Byte ef_enable = data.get(enableField, PersistentDataType.BYTE);
                    if (ef_enable == 1) {
                        data.set(enableField, PersistentDataType.BYTE, (byte) 0);
                        sendPluginMessage("messages.replanting-not-works", p);
                    } else {
                        data.set(enableField, PersistentDataType.BYTE, (byte) 1);
                        sendPluginMessage("messages.replanting-works", p);
                    }
                }
            }

            case ("reload") -> {
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    if (sender.isOp()){
                        plugin.reloadConfig();
                        config = plugin.getConfig();
                        sendPluginMessage("messages.config-reloaded", player);
                        Bukkit.getLogger().info("EasyFarming config is reloaded!");
                    }else{
                        sendPluginMessage("messages.no-permission", player);
                    }
                }else if (sender.isOp()){
                    plugin.reloadConfig();
                    config = plugin.getConfig();
                    Bukkit.getLogger().info("EasyFarming config is reloaded!");
                }
            }

            default -> {
            }
        }

        return true;
    }
}
