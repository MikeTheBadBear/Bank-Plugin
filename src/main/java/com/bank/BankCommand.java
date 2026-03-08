package com.example.bank;

import com.example.bank.citizens.BankerTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand implements CommandExecutor {

    private final com.example.bank.BankPlugin plugin;

    public BankCommand(com.example.bank.BankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getGuiManager().openGUI(new BankGUI(plugin), player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit":
                if (args.length < 2) {
                    player.sendMessage("Usage: /bank deposit <amount>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    plugin.getBankManager().deposit(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid amount.");
                }
                break;
            case "withdraw":
                if (args.length < 2) {
                    player.sendMessage("Usage: /bank withdraw <amount>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    plugin.getBankManager().withdraw(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid amount.");
                }
                break;
            case "balance":
                double balance = plugin.getBankManager().getBalance(player.getUniqueId());
                player.sendMessage("Your bank balance: $" + balance);
                break;
            case "reload":
                if (!player.hasPermission("bank.admin")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                plugin.reloadPlugin();
                player.sendMessage("Config reloaded.");
                break;
            case "setinterest":
                if (!player.hasPermission("bank.admin")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("Usage: /bank setinterest <rate>");
                    return true;
                }
                try {
                    double rate = Double.parseDouble(args[1]);
                    plugin.getConfigManager().setInterestRate(rate);
                    player.sendMessage("Interest rate set to " + rate);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid rate.");
                }
                break;
            case "depositexp":
                if (!player.hasPermission("bank.exp.deposit")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("Usage: /bank depositexp <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    plugin.getBankManager().depositExp(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid amount.");
                }
                break;
            case "withdrawexp":
                if (!player.hasPermission("bank.exp.withdraw")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("Usage: /bank withdrawexp <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    plugin.getBankManager().withdrawExp(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid amount.");
                }
                break;
            case "expbalance":
                if (!player.hasPermission("bank.exp.balance")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                int exp = plugin.getBankManager().getStoredExp(player.getUniqueId());
                player.sendMessage("Your stored exp: " + exp);
                break;
            case "depositallxp":
                if (!player.hasPermission("bank.exp.depositall")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                plugin.getBankManager().depositAllExp(player);
                break;
            case "setnpc":
                if (!player.hasPermission("bank.admin.setnpc")) {
                    player.sendMessage("You don't have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("Usage: /bank setnpc <npcid>");
                    return true;
                }
                try {
                    int npcId = Integer.parseInt(args[1]);
                    NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
                    if (npc == null) {
                        player.sendMessage("NPC with ID " + npcId + " not found.");
                        return true;
                    }
                    if (!npc.hasTrait(BankerTrait.class)) {
                        npc.addTrait(BankerTrait.class);
                        player.sendMessage("NPC " + npcId + " is now a banker.");
                    } else {
                        player.sendMessage("NPC " + npcId + " is already a banker.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid NPC ID.");
                }
                break;
            default:
                player.sendMessage("Unknown subcommand.");
                break;
        }

        return true;
    }
}