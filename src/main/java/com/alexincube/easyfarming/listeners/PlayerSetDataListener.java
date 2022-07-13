package com.alexincube.easyfarming.listeners;

import com.alexincube.easyfarming.easyfarming;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PlayerSetDataListener implements Listener {
    Plugin plugin = easyfarming.getInstance();
    FileConfiguration config = plugin.getConfig();

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        Player p = event.getPlayer();

        PersistentDataContainer PlayerData = p.getPersistentDataContainer();

        NamespacedKey enableField = new NamespacedKey(plugin, "ef_enable");
        if (!PlayerData.has(enableField, PersistentDataType.BYTE)){
            byte data = config.getBoolean("crop-replanting-and-harvesting-by-default") ? (byte) 1 : (byte) 0;
            PlayerData.set(enableField, PersistentDataType.BYTE, data);
        }
    }
}
