package com.trovetreasurer.commands;

import com.trovetreasurer.TroveTreasurer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TTCommands implements CommandExecutor {

    private final TroveTreasurer plugin;

    public TTCommands(TroveTreasurer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bal") || command.getName().equalsIgnoreCase("balance")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Economy econ = TroveTreasurer.getEconomy();
                if (econ == null) {
                    player.sendMessage("Economy service is not available.");
                    return true;
                }
                double inventoryBalance = getInventoryBalance(player, TroveTreasurer.getCurrencyItem());
                double troveBalance = getTroveBalance(player, TroveTreasurer.getCurrencyItem());
                double totalBalance = inventoryBalance + troveBalance;
                player.sendMessage("Inventory Balance: " + inventoryBalance);
                player.sendMessage("Trove Balance: " + troveBalance);
                player.sendMessage("Total Balance: " + totalBalance);
                return true;
            }
        }
        return false;
    }

    private int getInventoryBalance(Player player, Material currencyItem) {
        int balance = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currencyItem) {
                balance += item.getAmount();
            }
        }
        return balance;
    }

    private int getTroveBalance(Player player, Material currencyItem) {
        Block trove = plugin.getTrove(player.getUniqueId());
        if (trove == null || !(trove.getState() instanceof Container)) {
            return 0;
        }
        Container container = (Container) trove.getState();
        int balance = 0;
        for (ItemStack item : container.getInventory().getContents()) {
            if (item != null && item.getType() == currencyItem) {
                balance += item.getAmount();
            }
        }
        return balance;
    }
}