package com.alexincube.easyfarming.listeners;

import com.alexincube.easyfarming.easyfarming;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Iterator;

public class ReplantingListener implements Listener {
    private final HashMap<Material, Material> cropsAndSeeds = new HashMap<>(){{
        put(Material.WHEAT, Material.WHEAT_SEEDS);
        put(Material.CARROTS, Material.CARROT);
        put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        put(Material.POTATOES, Material.POTATO);
        put(Material.NETHER_WART, Material.NETHER_WART);
    }};
    Plugin pluginInstance = easyfarming.getInstance();

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BlockBreakEvent(BlockBreakEvent event){
        FileConfiguration config = pluginInstance.getConfig();
        if (!config.getBoolean("crop-replant-requires-hoe")) return;

        Player player = event.getPlayer();
        if (config.getBoolean("plugin-required-permission")) {
            if (!player.hasPermission("ef.replanting")) {
                return;
            }
        }

        // If player enable replanting
        if(!isPlayerEnableReplanting(player)) return;
        // If player has hoe
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Material toolMaterial = itemInHand.getType();
        if (!toolMaterial.toString().endsWith("HOE")) return;

        // If block is crop
        Block brokenBlock = event.getBlock();
        BlockData brokenBlockData = brokenBlock.getBlockData();
        Material brokenMaterial = brokenBlockData.getMaterial();
        if (!cropsAndSeeds.containsKey(brokenMaterial)) return;

        // If crop is grown
        if (!isFullyGrown(brokenBlock)) return;
        // Get crop seed
        Material seed = getSeedMaterial(brokenMaterial);
        if (seed == null) return;

        consumeSeed(player, seed, brokenBlock);
        Location loc = brokenBlock.getLocation();
        //Set broken crop to new the same crop
        Bukkit.getScheduler().runTaskLater(easyfarming.getInstance(), () -> loc.getBlock().setType(brokenMaterial), 1);
    }

    @EventHandler
    public void PlayerInteractEvent (PlayerInteractEvent event){
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        FileConfiguration config = pluginInstance.getConfig();
        if (config.getBoolean("crop-replant-requires-hoe")) return;

        Player player = event.getPlayer();

        if (config.getBoolean("plugin-required-permission")) {
            if (!player.hasPermission("ef.replanting")) {
                return;
            }
        }

        if(!isPlayerEnableReplanting(player))return;

        Block interactedBlock = event.getClickedBlock();
        if (interactedBlock == null) return;
        BlockData interactedBlockData = interactedBlock.getBlockData();

        Material brokenMaterial = interactedBlockData.getMaterial();
        if (!cropsAndSeeds.containsKey(brokenMaterial)) return;
        if (!isFullyGrown(interactedBlock)) return;

        if (config.getBoolean("crop-harvesting-prevent-afk-farm")){
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.BONE_MEAL) return;
        }

        Material interactedMaterial = interactedBlockData.getMaterial();

        if (!cropsAndSeeds.containsKey(interactedMaterial)) return;

        Material seed = getSeedMaterial(interactedMaterial);

        if (seed == null) return;

        consumeSeed(player, seed, interactedBlock);

        player.swingMainHand();

        interactedBlock.setType(interactedMaterial);
    }

    public boolean isPlayerEnableReplanting(Player p){
        PersistentDataContainer data = p.getPersistentDataContainer();

        NamespacedKey enableField = new NamespacedKey(pluginInstance, "ef_enable");

        if (data.has(enableField, PersistentDataType.BYTE)){
            Byte ef_enable = data.get(enableField, PersistentDataType.BYTE);
            return ef_enable == 1;
        }
        return false;
    }
    public boolean isFullyGrown(Block block){
        Ageable age = (Ageable) block.getBlockData();
        return age.getMaximumAge() == age.getAge();
    }

    public Material getSeedMaterial(Material cropType){
        return cropsAndSeeds.get(cropType);
    }

    public void consumeSeed(Player player, Material seed, Block crop){
        World world = player.getWorld();
        Location loc = crop.getLocation();

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        Iterator<ItemStack> drop = crop.getDrops(heldItem).iterator();

        boolean removedItem = false;
        while(drop.hasNext()) {
            ItemStack item = drop.next();

            if (item.getType().equals(Material.AIR)) continue;
            if (!removedItem && item.getType().equals(seed)) {
                Bukkit.getLogger().info("seed total drops: " + item.getAmount());
                item.setAmount(item.getAmount() - 1);

                removedItem = true;
            }
            if (item.getAmount() < 1) continue;
            world.dropItemNaturally(loc, item);
        }


    }
}
