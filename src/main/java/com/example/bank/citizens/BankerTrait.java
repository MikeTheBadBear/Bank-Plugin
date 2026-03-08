package com.example.bank.citizens;

import com.example.bank.BankPlugin;
import com.example.bank.BankGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@TraitName("bankertrait")
public class BankerTrait extends Trait {

    private BankPlugin plugin;

    public BankerTrait() {
        super("bankertrait");
    }

    @Override
    public void onAttach() {
        plugin = (BankPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("BankPlugin");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(getNPC())) {
            return;
        }
        Player player = event.getClicker();
        if (!player.hasPermission("bank.use")) {
            player.sendMessage("§cYou don't have permission to use the bank.");
            return;
        }
        plugin.getGuiManager().openGUI(new BankGUI(plugin), player);
    }
}