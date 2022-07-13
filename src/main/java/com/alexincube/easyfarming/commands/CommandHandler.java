package com.alexincube.easyfarming.commands;

import com.alexincube.easyfarming.easyfarming;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class CommandHandler implements CommandExecutor{
    Plugin plugin = easyfarming.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return true;
        switch (args[0]){
            case ("toggle"):
                if (sender instanceof Player p){
                    PersistentDataContainer data = p.getPersistentDataContainer();

                    NamespacedKey enableField = new NamespacedKey(plugin, "ef_enable");

                    if (data.has(enableField, PersistentDataType.BYTE)){
                        Byte ef_enable = data.get(enableField, PersistentDataType.BYTE);
                        if (ef_enable == 1){
                            data.set(enableField, PersistentDataType.BYTE, (byte) 0);
                            p.sendMessage("[EasyFarming] Теперь на вас не действует пересаживание растений");
                        }else{
                            data.set(enableField, PersistentDataType.BYTE, (byte) 1);
                            p.sendMessage("[EasyFarming] Теперь вы можете пересаживать растения");
                        }
                    }
                }
                break;

            case ("reload"):
                plugin.reloadConfig();
                break;

            default:
                break;
        }

        return true;
    }
}
