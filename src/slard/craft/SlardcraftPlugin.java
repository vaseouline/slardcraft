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
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;

public class SlardcraftPlugin extends JavaPlugin {
    // Map of banned crafting items and their legal counterpart. null if no legal
    // counterpart
    public static boolean DEBUG;
    public static Map<Material, Material> BANNED_CRAFT_MAP = Map.ofEntries(
            entry(Material.DIAMOND_AXE, Material.IRON_AXE),
            entry(Material.DIAMOND_BOOTS, Material.IRON_BOOTS),
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
        if (System.getenv("SLARD_DEBUG") == null) {
            DEBUG = false;
        }
        DEBUG = Boolean.parseBoolean(System.getenv("SLARD_DEBUG"));
        sanitizeRecipes();
        addRecipes();
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerFoodListener(), this);
        getServer().getPluginManager().registerEvents(new SmithingListener(), this);
        if (SlardcraftPlugin.DEBUG) this.getCommand("cheat").setExecutor(new CheatCommand());
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

            if (SlardcraftPlugin.BANNED_CRAFT_SET.contains(recipe.getResult().getType()))
                recipes.remove();
        }
    }

    private void addRecipes() {
        
        Recipe bigIronBreakDownRecipe = BigOre.getBigOreBreakDownRecipe(new NamespacedKey(this, "big_iron"), Material.RAW_IRON);
        Recipe megaIronBreakDownRecipe = BigOre.getMegaOreBreakDownRecipe(new NamespacedKey(this, "mega_iron"), Material.RAW_IRON);
        Recipe bigGoldBreakDownRecipe = BigOre.getBigOreBreakDownRecipe(new NamespacedKey(this, "big_gold"), Material.RAW_GOLD);
        Recipe megaGoldBreakDownRecipe = BigOre.getMegaOreBreakDownRecipe(new NamespacedKey(this, "mega_gold"), Material.RAW_GOLD);
        Recipe bigDiamondBreakDownRecipe = BigOre.getBigOreBreakDownRecipe(new NamespacedKey(this, "big_diamond"), Material.DIAMOND);
        Recipe megaDiamondBreakDownRecipe = BigOre.getMegaOreBreakDownRecipe(new NamespacedKey(this, "mega_diamond"), Material.DIAMOND);
        Recipe[] bigOreRecipes = {
            bigIronBreakDownRecipe,
            megaIronBreakDownRecipe,
            bigGoldBreakDownRecipe,
            megaGoldBreakDownRecipe,
            bigDiamondBreakDownRecipe,
            megaDiamondBreakDownRecipe};
        for (Recipe r : bigOreRecipes) {
            getServer().addRecipe(r);
        }
        getServer().addRecipe(PlayerFoodListener.getSeasonedMeatRecipe(new NamespacedKey(this, "seasoned_steak"), Material.COOKED_BEEF));
        getServer().addRecipe(PlayerFoodListener.getSeasonedMeatRecipe(new NamespacedKey(this, "seasoned_porkchop"), Material.COOKED_PORKCHOP));
        getServer().addRecipe(PlayerFoodListener.getFancySugarRecipe(new NamespacedKey(this, "fancy_sugar")));
        getServer().addRecipe(PlayerFoodListener.getFancyCookieRecipe(new NamespacedKey(this, "fancy_cookie")));
        getServer().addRecipe(CoatedPickaxe.getDiamondCoatedPickaxeRecipe(new NamespacedKey(this, "coated_pickaxe")));
    }

}