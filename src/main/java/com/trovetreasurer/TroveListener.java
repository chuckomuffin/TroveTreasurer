package com.trovetreasurer.listeners;

import com.trovetreasurer.TroveTreasurer;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class TroveListener implements Listener {

    private final TroveTreasurer plugin;

    public TroveListener(TroveTreasurer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            ItemMeta meta = event.getItemInHand().getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                String playerName = event.getPlayer().getName();
                if (displayName.equalsIgnoreCase(playerName + "'s Trove")) {
                    UUID playerUUID = event.getPlayer().getUniqueId();
                    if (plugin.getTrove(playerUUID) != null) {
                        event.getPlayer().sendMessage("You already have a trove.");
                    } else {
                        plugin.setTrove(playerUUID, block);
                        saveTroveToConfig(playerUUID, block);
                        event.getPlayer().sendMessage("Your trove has been created.");
                    }
                }
            }
        }
    }

    private void saveTroveToConfig(UUID playerUUID, Block block) {
        FileConfiguration config = plugin.getTrovesConfig();
        config.set("troves." + playerUUID.toString() + ".world", block.getWorld().getName());
        config.set("troves." + playerUUID.toString() + ".x", block.getX());
        config.set("troves." + playerUUID.toString() + ".y", block.getY());
        config.set("troves." + playerUUID.toString() + ".z", block.getZ());
        plugin.saveTrovesConfig();
    }
}