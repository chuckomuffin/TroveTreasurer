//Main plugin file
package com.trovetreasurer;

import org.bukkit.plugin.java.JavaPlugin;

public class TroveTreasurer extends JavaPlugin {
    private TroveEconomy economy;
    private TroveExchange troveExchange;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.troveExchange = new TroveExchange(this);
        economy = new TroveEconomy(this);
        getServer().getPluginManager().registerEvents(new TroveListener(this), this);
        getCommand("bal").setExecutor(new TTCommands(this));
        getCommand("balance").setExecutor(new TTCommands(this));
        getCommand("send").setExecutor(new TTCommands(this));
        getCommand("reload").setExecutor(new TTCommands(this));
    }

    public TroveEconomy getEconomy() {
        return economy;
    }

    public TroveExchange getTroveExchange() {
        return troveExchange;
    }
}