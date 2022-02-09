package slard.craft;

import static java.util.Map.entry;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import slard.craft.town.GameState;
import slard.craft.town.GameStateListener;

public class SlardcraftPlugin extends JavaPlugin {
    // Map of banned crafting items and their legal counterpart. null if no legal
    // counterpart
    public static boolean DEBUG;
    public static Map<Material, Material> BANNED_CRAFT_MAP = Map.ofEntries(
            entry(Material.DIAMOND_AXE, Material.IRON_AXE),
            entry(Material.DIAMOND_SWORD, Material.IRON_SWORD),
            entry(Material.NETHERITE_AXE, Material.IRON_AXE),
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
        getServer().getPluginManager().registerEvents(new GameStateListener(this), this);
        GameState.initializeGameState();


        if (SlardcraftPlugin.DEBUG)
            this.getCommand("cheat").setExecutor(new CheatCommand());
        this.getCommand("slard").setExecutor(new SlardCommand());
    }

    // Fired when plugin is disabled
    @Override
    public void onDisable() {
        GameState.saveGameState();
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

        getServer().addRecipe(PlayerFoodListener.getSeasonedMeatRecipe(new NamespacedKey(this, "seasoned_steak"),
                Material.COOKED_BEEF));
        getServer().addRecipe(PlayerFoodListener.getSeasonedMeatRecipe(new NamespacedKey(this, "seasoned_porkchop"),
                Material.COOKED_PORKCHOP));
        getServer().addRecipe(PlayerFoodListener.getFancySugarRecipe(new NamespacedKey(this, "fancy_sugar")));
        getServer().addRecipe(PlayerFoodListener.getFancyCookieRecipe(new NamespacedKey(this, "fancy_cookie")));
    }

}