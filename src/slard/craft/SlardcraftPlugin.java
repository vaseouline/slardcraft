package slard.craft;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.Map.entry;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmithingRecipe;

public class SlardcraftPlugin extends JavaPlugin {
    // Map of banned crafting items and their legal counterpart. null if no legal
    // counterpart

    public static boolean DEBUG = true;
    public static Map<Material, Material> BANNED_CRAFT_MAP = Map.ofEntries(
            entry(Material.DIAMOND_AXE, Material.IRON_AXE),
            entry(Material.DIAMOND_CHESTPLATE, Material.IRON_CHESTPLATE),
            entry(Material.DIAMOND_HELMET, Material.IRON_HELMET), 
            entry(Material.DIAMOND_HOE, Material.IRON_HOE),
            entry(Material.DIAMOND_LEGGINGS, Material.IRON_LEGGINGS),
            entry(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE),
            entry(Material.DIAMOND_SHOVEL, Material.IRON_SHOVEL), 
            entry(Material.DIAMOND_SWORD, Material.IRON_SWORD),
            entry(Material.NETHERITE_AXE, Material.IRON_AXE),
            entry(Material.NETHERITE_BOOTS, Material.IRON_BOOTS),
            entry(Material.NETHERITE_CHESTPLATE, Material.IRON_CHESTPLATE),
            entry(Material.NETHERITE_HELMET, Material.IRON_HELMET), 
            entry(Material.NETHERITE_HOE, Material.IRON_HOE),
            entry(Material.NETHERITE_LEGGINGS, Material.IRON_LEGGINGS),
            entry(Material.NETHERITE_PICKAXE, Material.IRON_PICKAXE),
            entry(Material.NETHERITE_SHOVEL, Material.IRON_SHOVEL),
            entry(Material.NETHERITE_SWORD, Material.IRON_SWORD), 
            entry(Material.ELYTRA, Material.PHANTOM_MEMBRANE));
    public static Set<Material> BANNED_CRAFT_SET = BANNED_CRAFT_MAP.keySet();

    public SlardcraftPlugin() {

    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        Bukkit.getWorld("world").setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 50);
        Bukkit.getWorld("world").setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        Bukkit.getWorld("world").setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        sanitizeRecipes();
    }

    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }

    private void sanitizeRecipes() {
        Iterator<Recipe> recipes = getServer().recipeIterator();
        Recipe recipe;
        while (recipes.hasNext()) {
            recipe = recipes.next();
            if (recipe == null) {
                continue;
            }

            if (recipe instanceof ShapedRecipe && SlardcraftPlugin.BANNED_CRAFT_SET.contains(recipe.getResult().getType()))
                recipes.remove();
        }
    }

}