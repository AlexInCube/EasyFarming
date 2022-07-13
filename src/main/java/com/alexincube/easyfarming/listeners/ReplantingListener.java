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
    HashMap<Material, Material> cropsAndSeeds = new HashMap<>();
    Plugin pluginInstance = easyfarming.getInstance();
    FileConfiguration config = pluginInstance.getConfig();

    public ReplantingListener(){
        cropsAndSeeds.put(Material.WHEAT, Material.WHEAT_SEEDS);
        cropsAndSeeds.put(Material.CARROTS, Material.CARROT);
        cropsAndSeeds.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        cropsAndSeeds.put(Material.POTATOES, Material.POTATO);
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event){
        Player player = event.getPlayer();

        if(!isPlayerEnableReplanting(player))return;
        Block brokenBlock = event.getBlock();
        BlockData brokenBlockData = brokenBlock.getBlockData();
        // If block is crop
        if (!(brokenBlockData instanceof Ageable)) return;

        Material brokenMaterial = brokenBlockData.getMaterial();
        Location loc = brokenBlock.getLocation();

        // If crop is grown
        if (!isFullyGrown(brokenBlock)) return;

        if (config.getBoolean("crop-replant-requires-hoe")){
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Material toolMaterial = itemInHand.getType();
            if (!toolMaterial.toString().endsWith("HOE")) return;
        }

        Material seed = getSeedMaterial(brokenMaterial);

        if (seed == null) return;
        //If player have seeds
        if (!isItemInInventory(player, seed)) return;

        consumeSeed(player, seed);

        //Set broken crop to new the same crop
        Bukkit.getScheduler().runTaskLater(easyfarming.getInstance(), () -> loc.getBlock().setType(brokenMaterial), 2);
    }

    @EventHandler
    public void PlayerInteractEvent (PlayerInteractEvent event){
        Player player = event.getPlayer();

        if(!isPlayerEnableReplanting(player))return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block interactedBlock = event.getClickedBlock();

        if (interactedBlock == null) return;
        BlockData interactedBlockData = interactedBlock.getBlockData();

        if (!(interactedBlockData instanceof Ageable)) return;
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
            if (ef_enable == 1){
                return true;
            }else{
                return false;
            }
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
