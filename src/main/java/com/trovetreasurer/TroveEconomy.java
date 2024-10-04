package com.trovetreasurer;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TroveEconomy extends AbstractEconomy {
    private final TroveTreasurer plugin;
    private final File balancesFile;
    private final FileConfiguration balancesConfig;

    public TroveEconomy(TroveTreasurer plugin) {
        this.plugin = plugin;
        this.balancesFile = new File(plugin.getDataFolder(), "balances.yml");
        this.balancesConfig = YamlConfiguration.loadConfiguration(balancesFile);
        plugin.getServer().getServicesManager().register(Economy.class, this, plugin, ServicePriority.Highest);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "TroveEconomy";
    }

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
        return String.format("%.0f %s", amount, currencyNamePlural());
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("currency.plural", "Coins");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("currency.singular", "Coin");
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return balancesConfig.contains("accounts." + player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return balancesConfig.getDouble("accounts." + player.getUniqueId() + ".balance", 0);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        double balance = getBalance(player);
        if (balance < amount) {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        balancesConfig.set("accounts." + player.getUniqueId() + ".balance", balance - amount);
        saveBalancesConfig();
        return new EconomyResponse(amount, balance - amount, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        double balance = getBalance(player);
        balancesConfig.set("accounts." + player.getUniqueId() + ".balance", balance + amount);
        saveBalancesConfig();
        return new EconomyResponse(amount, balance + amount, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (hasAccount(player)) {
            return false;
        }

        balancesConfig.set("accounts." + player.getUniqueId() + ".balance", 0);
        saveBalancesConfig();
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        // Implement this method if needed
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        // Implement this method if needed
        return false;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse createBank(String name, String worldName) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String worldName) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, String worldName) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not implemented");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Player-specific accounts are not implemented");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "World-specific accounts are not implemented");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "World-specific accounts are not implemented");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        // Implement this method if needed
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Player-specific accounts are not implemented");
    }

    @Override
    public boolean hasAccount(String playerName) {
        // Implement this method if needed
        return false;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        // Implement this method if needed
        return false;
    }

    @Override
    public boolean has(String playerName, double amount) {
        // Implement this method if needed
        return false;
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        // Implement this method if needed
        return 0;
    }

    @Override
    public double getBalance(String playerName) {
        // Implement this method if needed
        return 0;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        // Implement this method if needed
        return false;
    }

    private void saveBalancesConfig() {
        try {
            balancesConfig.save(balancesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}