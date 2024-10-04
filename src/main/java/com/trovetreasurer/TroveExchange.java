package com.trovetreasurer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TroveExchange {
    private final TroveTreasurer plugin;
    private final File trovesFile;
    private final File balancesFile;
    private final FileConfiguration trovesConfig;
    private final FileConfiguration balancesConfig;

    public TroveExchange(TroveTreasurer plugin) {
        this.plugin = plugin;
        this.trovesFile = new File(plugin.getDataFolder(), "troves.yml");
        this.balancesFile = new File(plugin.getDataFolder(), "balances.yml");
        this.trovesConfig = YamlConfiguration.loadConfiguration(trovesFile);
        this.balancesConfig = YamlConfiguration.loadConfiguration(balancesFile);
    }

    public void updateBalancesFromTroves() {
        for (String playerUUID : trovesConfig.getConfigurationSection("troves").getKeys(false)) {
            UUID uuid = UUID.fromString(playerUUID);
            int balance = 0;

            for (String key : trovesConfig.getConfigurationSection("troves." + playerUUID + ".contents").getKeys(false)) {
                ItemStack item = trovesConfig.getItemStack("troves." + playerUUID + ".contents." + key);
                if (item != null) {
                    int value = getItemValue(item);
                    balance += item.getAmount() * value;
                }
            }

            balancesConfig.set("accounts." + playerUUID + ".balance", balance);
        }

        saveBalancesConfig();
    }

    public void updateTrovesFromBalances() {
        for (String playerUUID : balancesConfig.getConfigurationSection("accounts").getKeys(false)) {
            UUID uuid = UUID.fromString(playerUUID);
            int balance = balancesConfig.getInt("accounts." + playerUUID + ".balance");

            for (String key : trovesConfig.getConfigurationSection("troves." + playerUUID + ".contents").getKeys(false)) {
                ItemStack item = trovesConfig.getItemStack("troves." + playerUUID + ".contents." + key);
                if (item != null) {
                    int value = getItemValue(item);
                    int amount = balance / value;
                    balance -= amount * value;

                    item.setAmount(amount);
                    trovesConfig.set("troves." + playerUUID + ".contents." + key, item);
                }
            }
        }

        saveTrovesConfig();
    }

    private int getItemValue(ItemStack item) {
        String itemType = item.getType().toString();
        FileConfiguration config = plugin.getConfig();
        if (config.contains("currency-items." + itemType)) {
            return config.getInt("currency-items." + itemType + ".value", 0);
        }
        return 0;
    }

    private void saveTrovesConfig() {
        try {
            trovesConfig.save(trovesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBalancesConfig() {
        try {
            balancesConfig.save(balancesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}