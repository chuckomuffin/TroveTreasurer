package com.trovetreasurer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TTCommands implements CommandExecutor {
    private final TroveTreasurer plugin;

    public TTCommands(TroveTreasurer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("bal") || label.equalsIgnoreCase("balance")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                double balance = plugin.getEconomy().getBalance(player);
                player.sendMessage("Your balance is: " + balance + " " + plugin.getConfig().getString("currency.plural"));
            } else {
                sender.sendMessage("This command can only be used by players.");
            }
            return true;
        }

        if (label.equalsIgnoreCase("tt")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /tt <send|reload|account>");
                return true;
            }

            if (args[0].equalsIgnoreCase("send")) {
                if (args.length != 3) {
                    sender.sendMessage("Usage: /tt send <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }

                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid amount.");
                    return true;
                }

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    double balance = plugin.getEconomy().getBalance(player);
                    if (balance < amount) {
                        player.sendMessage("Insufficient funds.");
                        return true;
                    }

                    plugin.getEconomy().withdrawPlayer(player, amount);
                    plugin.getEconomy().depositPlayer(target, amount);
                    player.sendMessage("Sent " + amount + " " + plugin.getConfig().getString("currency.plural") + " to " + target.getName());
                    target.sendMessage("Received " + amount + " " + plugin.getConfig().getString("currency.plural") + " from " + player.getName());
                } else {
                    sender.sendMessage("This command can only be used by players.");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("Plugin reloaded.");
                return true;
            }

            if (args[0].equalsIgnoreCase("account")) {
                if (args.length != 2) {
                    sender.sendMessage("Usage: /tt account <player>");
                    return true;
                }

                if (!sender.hasPermission("trovetreasurer.account")) {
                    sender.sendMessage("You do not have permission to use this command.");
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                double balance = plugin.getEconomy().getBalance(target);
                sender.sendMessage(target.getName() + "'s balance is: " + balance + " " + plugin.getConfig().getString("currency.plural"));
                return true;
            }
        }

        return false;
    }
}