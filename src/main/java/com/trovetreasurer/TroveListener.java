package com.trovetreasurer;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TroveListener implements Listener {
    private final TroveTreasurer plugin;
    private final TroveExchange troveExchange;
    private final Set<UUID> playersWithTroves;

    public TroveListener(TroveTreasurer plugin) {
        this.plugin = plugin;
        this.troveExchange = plugin.getTroveExchange();
        this.playersWithTroves = new HashSet<>();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("[Trove]")) {
                UUID playerUUID = player.getUniqueId();

                if (playersWithTroves.contains(playerUUID)) {
                    player.sendMessage("You already have a Trove.");
                    event.setCancelled(true);
                } else {
                    playersWithTroves.add(playerUUID);
                    player.sendMessage("Trove created!");
                    updateTrovesYml(playerUUID, block, "create");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getState() instanceof Container) {
            UUID playerUUID = player.getUniqueId();

            if (playersWithTroves.contains(playerUUID)) {
                playersWithTroves.remove(playerUUID);
                player.sendMessage("Trove destroyed!");
                updateTrovesYml(playerUUID, block, "destroy");
                troveExchange.updateTrovesFromBalances();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null && block.getState() instanceof Container) {
            UUID playerUUID = player.getUniqueId();

            if (playersWithTroves.contains(playerUUID)) {
                player.sendMessage("Trove opened!");
                updateTrovesYml(playerUUID, block, "open");
                troveExchange.updateBalancesFromTroves();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        Block block = player.getTargetBlockExact(5); // Assuming the player is looking at the Trove

        if (block != null && block.getState() instanceof Container) {
            UUID playerUUID = player.getUniqueId();

            if (playersWithTroves.contains(playerUUID)) {
                updateTrovesYml(playerUUID, block, "close", inventory);
                troveExchange.updateTrovesFromBalances();
                player.sendMessage("Trove closed and contents saved!");
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        Block block = player.getTargetBlockExact(5); // Assuming the player is looking at the Trove

        if (block != null && block.getState() instanceof Container) {
            UUID playerUUID = player.getUniqueId();

            if (playersWithTroves.contains(playerUUID)) {
                updateTrovesYml(playerUUID, block, "open", inventory);
                troveExchange.updateBalancesFromTroves();
                player.sendMessage("Trove opened and contents loaded!");
            }
        }
    }

    private void updateTrovesYml(UUID playerUUID, Block block, String action) {
        updateTrovesYml(playerUUID, block, action, null);
    }

    private void updateTrovesYml(UUID playerUUID, Block block, String action, Inventory inventory) {
        FileConfiguration trovesConfig = plugin.getConfig();
        String path = "troves." + playerUUID.toString();

        switch (action) {
            case "create":
                trovesConfig.set(path + ".location", block.getLocation().toString());
                break;
            case "destroy":
                trovesConfig.set(path, null);
                break;
            case "open":
                if (inventory != null) {
                    loadTroveContents(playerUUID, inventory);
                }
                break;
            case "close":
                if (inventory != null) {
                    saveTroveContents(playerUUID, inventory);
                }
                break;
        }

        plugin.saveConfig();
    }

    private void saveTroveContents(UUID playerUUID, Inventory inventory) {
        FileConfiguration trovesConfig = plugin.getConfig();
        String path = "troves." + playerUUID.toString() + ".contents";

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                trovesConfig.set(path + "." + i, item);
            } else {
                trovesConfig.set(path + "." + i, null);
            }
        }
    }

    private void loadTroveContents(UUID playerUUID, Inventory inventory) {
        FileConfiguration trovesConfig = plugin.getConfig();
        String path = "troves." + playerUUID.toString() + ".contents";

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = trovesConfig.getItemStack(path + "." + i);
            inventory.setItem(i, item);
        }
    }
}