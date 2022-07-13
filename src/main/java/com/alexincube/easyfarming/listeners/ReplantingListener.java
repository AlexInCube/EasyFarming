package com.alexincube.easyfarming.listeners;

import com.alexincube.easyfarming.easyfarming;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

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

        // If player enable replanting
        Player player = event.getPlayer();
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
        //If player have seeds in inventory
        if (!isItemInInventory(player, seed)) return;

        consumeSeed(player, seed);
        Location loc = brokenBlock.getLocation();
        //Set broken crop to new the same crop
        Bukkit.getScheduler().runTaskLater(easyfarming.getInstance(), () -> loc.getBlock().setType(brokenMaterial), 2);
    }

    @EventHandler
    public void PlayerInteractEvent (PlayerInteractEvent event){
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        FileConfiguration config = pluginInstance.getConfig();
        if (config.getBoolean("crop-replant-requires-hoe")) return;

        Player player = event.getPlayer();
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
        //If player have seeds
        if (!isItemInInventory(player, seed)) return;

        consumeSeed(player, seed);

        player.swingMainHand();

        Location loc = interactedBlock.getLocation();

        if(interactedBlock.breakNaturally()){
            Bukkit.getScheduler().runTaskLater(pluginInstance, () -> loc.getBlock().setType(interactedMaterial), 2);
        }
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

    public boolean isItemInInventory(Player player, Material cropType){
        PlayerInventory inventory = player.getInventory();
        return inventory.contains(cropType);
    }

    public void consumeSeed(Player player, Material seedType){
        int itemIndexLocation = -1;
        ItemStack currentItems;
        PlayerInventory inventory = player.getInventory();

        for (int slotIndex = 0; slotIndex < inventory.getSize(); slotIndex++){
            currentItems = inventory.getItem(slotIndex);

            if (currentItems == null) continue;

            if (currentItems.getType() == seedType){
                itemIndexLocation = slotIndex;
                break;
            }
        }

        if (itemIndexLocation != -1){
            ItemStack detectedItemStack = inventory.getItem(itemIndexLocation);
            if (detectedItemStack != null){
                int itemAmount = detectedItemStack.getAmount();
                detectedItemStack.setAmount(itemAmount - 1);
            }
        }
    }
}
