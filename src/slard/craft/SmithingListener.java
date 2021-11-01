package slard.craft;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SmithingListener implements Listener {

    private static List<Material> BANNED_SMITHING_LIST = Arrays.asList(
        Material.NETHERITE_AXE, 
        Material.NETHERITE_BOOTS, 
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_HELMET,
        Material.NETHERITE_HOE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_PICKAXE,
        Material.NETHERITE_SHOVEL,
        Material.NETHERITE_SWORD
    );

    private static Set<Material> BANNED_SMITHING_SET = new HashSet<>(BANNED_SMITHING_LIST);
    
    @EventHandler
    public void sanitizeSmithingTable(PrepareSmithingEvent event) {
        if (BANNED_SMITHING_SET.contains(event.getResult().getType())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void coatedPickaxeListener(PrepareSmithingEvent event) {
        if (!event.getResult().getType().equals(Material.DIAMOND_PICKAXE)) {
            return;
        }
        ItemStack ironPickaxe = event.getInventory().getItem(0);
        if (!ironPickaxe.getType().equals(Material.IRON_PICKAXE)) {
            return;
        }
        event.setResult(CoatedPickaxe.getCoatedPickaxe(ironPickaxe));
    }

    @EventHandler
    public void coatedPickaxeBreakListener(PlayerItemBreakEvent event) {
        if (CoatedPickaxe.isCoatedPickaxe(event.getBrokenItem())) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("COATED PICKAXE BROKE, DROPPING YOU YOUR ORIGINAL PICKAXE.");
            event.getPlayer().getInventory().addItem(CoatedPickaxe.getOriginalIronPickaxe(event.getBrokenItem()));
        }
    }
}
