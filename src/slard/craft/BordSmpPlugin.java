package slard.craft;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import static java.util.Map.entry;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

public class BordSmpPlugin extends JavaPlugin {
    public static boolean DEBUG;

    public static Map<Material, Material> BANNED_CRAFT_MAP = Map.ofEntries(
            entry(Material.SHULKER_BOX, Material.SHULKER_SHELL),
            entry(Material.ELYTRA, Material.PHANTOM_MEMBRANE),
            entry(Material.RESPAWN_ANCHOR, Material.CRYING_OBSIDIAN));
    public static Set<Material> BANNED_CRAFT_SET = BANNED_CRAFT_MAP.keySet();

    @Override
    public void onEnable() {
        if (System.getenv("SLARD_DEBUG") == null) {
            DEBUG = false;
        }
        DEBUG = Boolean.parseBoolean(System.getenv("SLARD_DEBUG"));
        sanitizeRecipes();
        getServer().getPluginManager().registerEvents(new DisableEnchantListener(), this);
        getServer().getPluginManager().registerEvents(new GameStateListener(this), this);

        this.getCommand("setradius").setExecutor(new SetRadiusCommand());
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

            if (BordSmpPlugin.BANNED_CRAFT_SET.contains(recipe.getResult().getType())) {
                recipes.remove();
            }
        }
    }

}