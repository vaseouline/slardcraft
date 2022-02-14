package slard.craft;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SmithingListener implements Listener {
//kind of pointless if assuming diamond swords and axes are also uncraftable
    private static List<Material> BANNED_SMITHING_LIST = Arrays.asList(
        Material.NETHERITE_AXE, 
        Material.NETHERITE_SWORD
    );

    private static Set<Material> BANNED_SMITHING_SET = new HashSet<>(BANNED_SMITHING_LIST);
    
    @EventHandler
    public void sanitizeSmithingTable(PrepareSmithingEvent event) {
        if (BANNED_SMITHING_SET.contains(event.getResult().getType())) {
            event.setResult(null);
        }
    }
}
