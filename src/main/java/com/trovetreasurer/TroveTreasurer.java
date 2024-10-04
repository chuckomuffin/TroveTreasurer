package com.trovetreasurer;

import com.trovetreasurer.commands.TTCommands;
import com.trovetreasurer.listeners.TroveListener;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

public class TroveTreasurer extends JavaPlugin implements Listener, Economy {

    private static Economy econ = null;
    private static Material currencyItem;
    private static String currencyNbtKey;
    private static String currencyNbtValue;
    private static String currencyNameSingular;
    private static String currencyNamePlural;
    private final Map<UUID, Block> troves = new HashMap<>();
    private final Map<UUID, List<ItemStack>> troveContents = new HashMap<>();

    private FileConfiguration trovesConfig = null;
    private File trovesConfigFile = null;

    @Override
public void onEnable() {
    saveDefaultConfig();
    loadTrovesFromConfig();
    getServer().getPluginManager().registerEvents(new TroveListener(this), this);
    getServer().getPluginManager().registerEvents(this, this);
    this.getCommand("bal").setExecutor(new TTCommands(this));
    this.getCommand("balance").setExecutor(new TTCommands(this));
    // Other initialization code
}

    private void reloadTrovesConfig() {
        if (trovesConfigFile == null) {
            trovesConfigFile = new File(getDataFolder(), "troves.yml");
        }
        trovesConfig = YamlConfiguration.loadConfiguration(trovesConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = getResource("troves.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            trovesConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getTrovesConfig() {
        if (trovesConfig == null) {
            reloadTrovesConfig();
        }
        return trovesConfig;
    }

    public void saveTrovesConfig() {
        if (trovesConfig == null || trovesConfigFile == null) {
            return;
        }
        try {
            getTrovesConfig().save(trovesConfigFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + trovesConfigFile, ex);
        }
    }

    private void loadTrovesFromConfig() {
        FileConfiguration config = getTrovesConfig();
        if (config.contains("troves")) {
            for (String key : config.getConfigurationSection("troves").getKeys(false)) {
                UUID playerUUID = UUID.fromString(key);
                String worldName = config.getString("troves." + key + ".world");
                int x = config.getInt("troves." + key + ".x");
                int y = config.getInt("troves." + key + ".y");
                int z = config.getInt("troves." + key + ".z");
                Block block = Bukkit.getWorld(worldName).getBlockAt(x, y, z);
                troves.put(playerUUID, block);

                if (config.contains("troves." + key + ".contents")) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) config.getList("troves." + key + ".contents");
                    List<ItemStack> itemStacks = new ArrayList<>();
                    for (Map<String, Object> item : items) {
                        itemStacks.add(ItemStack.deserialize(item));
                    }
                    troveContents.put(playerUUID, itemStacks);
                }
            }
        }
    }

    public void setTrove(UUID playerUUID, Block block) {
        troves.put(playerUUID, block);
        saveTroveToConfig(playerUUID, block);
    }

    public Block getTrove(UUID playerUUID) {
        return troves.get(playerUUID);
    }

    public void saveTroveToConfig(UUID playerUUID, Block block) {
        FileConfiguration config = getTrovesConfig();
        config.set("troves." + playerUUID.toString() + ".world", block.getWorld().getName());
        config.set("troves." + playerUUID.toString() + ".x", block.getX());
        config.set("troves." + playerUUID.toString() + ".y", block.getY());
        config.set("troves." + playerUUID.toString() + ".z", block.getZ());

        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            List<Map<String, Object>> items = new ArrayList<>();
            for (ItemStack item : container.getInventory().getContents()) {
                if (item != null) {
                    items.add(item.serialize());
                }
            }
            config.set("troves." + playerUUID.toString() + ".contents", items);
        }

        saveTrovesConfig();
    }

    private boolean setupEconomy() {
        // Setup economy code
        return econ != null;
    }

    @Override
    public void onDisable() {
        // Cleanup code
    }

    private void loadCurrencyItem() {
        // Load currency item code
    }

    private void loadCurrencyNames() {
        // Load currency names code
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Material getCurrencyItem() {
        return currencyItem;
    }

    public static String getCurrencyNbtKey() {
        return currencyNbtKey;
    }

    public static String getCurrencyNbtValue() {
        return currencyNbtValue;
    }

    public static String getCurrencyNameSingular() {
        return currencyNameSingular;
    }

    public static String getCurrencyNamePlural() {
        return currencyNamePlural;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Container) {
            Player player = (Player) event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            Block trove = getTrove(playerUUID);

            if (trove != null && trove.getState() instanceof Container) {
                Container container = (Container) trove.getState();
                List<ItemStack> storedContents = troveContents.get(playerUUID);
                if (storedContents != null) {
                    container.getInventory().setContents(storedContents.toArray(new ItemStack[0]));
                    troveContents.remove(playerUUID);
                }
            }
        }
    }

    // Implement Economy interface methods
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f", amount);
    }

    @Override
    public String currencyNamePlural() {
        return currencyNamePlural;
    }

    @Override
    public String currencyNameSingular() {
        return currencyNameSingular;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public double getBalance(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return 0;
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getPlayer());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player not found");
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getPlayer(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    private EconomyResponse withdrawPlayer(Player player, double amount) {
        if (player == null) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player not found");

        int amountToWithdraw = (int) amount;
        int inventoryBalance = getInventoryBalance(player, currencyItem);
        int troveBalance = getTroveBalance(player, currencyItem);
        int totalBalance = inventoryBalance + troveBalance;

        if (totalBalance < amountToWithdraw) {
            return new EconomyResponse(0, totalBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        int remainingAmount = amountToWithdraw;

        // Deduct from inventory first
        remainingAmount = deductFromInventory(player, currencyItem, remainingAmount);

        // Deduct from trove if needed
        if (remainingAmount > 0) {
            remainingAmount = deductFromTrove(player, currencyItem, remainingAmount);
        }

        return new EconomyResponse(amountToWithdraw, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
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
        Block trove = getTrove(player.getUniqueId());
        if (trove == null) {
            List<ItemStack> storedContents = troveContents.get(player.getUniqueId());
            if (storedContents != null) {
                int balance = 0;
                for (ItemStack item : storedContents) {
                    if (item != null && item.getType() == currencyItem) {
                        balance += item.getAmount();
                    }
                }
                return balance;
            }
            return 0;
        }
        if (!(trove.getState() instanceof Container)) {
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

    private int deductFromInventory(Player player, Material currencyItem, int amount) {
        int remainingAmount = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currencyItem) {
                int itemAmount = item.getAmount();
                if (itemAmount > remainingAmount) {
                    item.setAmount(itemAmount - remainingAmount);
                    remainingAmount = 0;
                    break;
                } else {
                    remainingAmount -= itemAmount;
                    item.setAmount(0);
                }
            }
        }
        return remainingAmount;
    }

    private int deductFromTrove(Player player, Material currencyItem, int amount) {
        Block trove = getTrove(player.getUniqueId());
        if (trove == null) {
            List<ItemStack> storedContents = troveContents.get(player.getUniqueId());
            if (storedContents != null) {
                int remainingAmount = amount;
                for (ItemStack item : storedContents) {
                    if (item != null && item.getType() == currencyItem) {
                        int itemAmount = item.getAmount();
                        if (itemAmount > remainingAmount) {
                            item.setAmount(itemAmount - remainingAmount);
                            remainingAmount = 0;
                            break;
                        } else {
                            remainingAmount -= itemAmount;
                            item.setAmount(0);
                        }
                    }
                }
                return remainingAmount;
            }
            return amount;
        }
        if (!(trove.getState() instanceof Container)) {
            return amount;
        }
        Container container = (Container) trove.getState();
        int remainingAmount = amount;
        for (ItemStack item : container.getInventory().getContents()) {
            if (item != null && item.getType() == currencyItem) {
                int itemAmount = item.getAmount();
                if (itemAmount > remainingAmount) {
                    item.setAmount(itemAmount - remainingAmount);
                    remainingAmount = 0;
                    break;
                } else {
                    remainingAmount -= itemAmount;
                    item.setAmount(0);
                }
            }
        }
        return remainingAmount;
    }
}