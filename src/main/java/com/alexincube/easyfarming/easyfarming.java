package com.alexincube.easyfarming;

import com.alexincube.easyfarming.commands.CommandHandler;
import com.alexincube.easyfarming.commands.CommandTabCompletion;
import com.alexincube.easyfarming.listeners.PlayerSetDataListener;
import com.alexincube.easyfarming.listeners.ReplantingListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class easyfarming extends JavaPlugin {
    private static Plugin instance;
    @Override
    public void onEnable(){
        instance = this;

        //Config Setup
        saveDefaultConfig();

        getLogger().info("enabled");

        //Commands Register
        this.getCommand("ef").setExecutor(new CommandHandler());
        this.getCommand("ef").setTabCompleter(new CommandTabCompletion());

        //Events Register
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new ReplantingListener(), instance);
        manager.registerEvents(new PlayerSetDataListener(), instance);
    }

    @Override
    public void onDisable(){
        getLogger().info("onDisable");
    }

    public static Plugin getInstance(){
        return instance;
    }
}
