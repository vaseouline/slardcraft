package slard.craft;

import static java.util.Map.entry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

class OreData {
    String name;
    ChatColor color;
    OreData(String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }
}

public class BigOre {
    public static final int BIG_COUNT = 3;

    public static Map<Material, OreData> oreMap = Map.ofEntries(
        entry(Material.RAW_GOLD, new OreData("Gold", ChatColor.YELLOW)),
        entry(Material.RAW_IRON, new OreData("Iron", ChatColor.GOLD)),
        entry(Material.DIAMOND, new OreData("Diamond", ChatColor.AQUA))
    );

    public static Set<Material> oreSet = oreMap.keySet();

    public static Set<ExactChoice> oreChoices;
    static {
        oreChoices = new HashSet<>();
        for (Material ore : oreSet) {
            oreChoices.add(new ExactChoice(getBigOre(ore)));
            oreChoices.add(new ExactChoice(getMegaOre(ore)));
        }
        
    }
    
    public static Recipe getBigOreBreakDownRecipe(NamespacedKey nms, Material ore) {
        if (!oreMap.containsKey(ore)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED BIG ORE MATERIAL: " + ore.toString());
            throw new IllegalArgumentException();
        }
        ItemStack output = new ItemStack(ore);
        output.setAmount(BIG_COUNT);
        ShapedRecipe recipe = new ShapedRecipe(nms, output);
        recipe.setGroup("o");
        recipe.shape("o");
        recipe.setIngredient('o', new ExactChoice(getBigOre(ore)));
        return recipe;
    }

    public static Recipe getMegaOreBreakDownRecipe(NamespacedKey nms, Material ore) {
        if (!oreMap.containsKey(ore)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED BIG ORE MATERIAL: " + ore.toString());
            throw new IllegalArgumentException();
        }
        ItemStack output = getBigOre(ore);
        output.setAmount(BIG_COUNT);
        ShapedRecipe recipe = new ShapedRecipe(nms, output);
        recipe.setGroup("o");
        recipe.shape("o");
        recipe.setIngredient('o', new ExactChoice(getMegaOre(ore)));
        return recipe;
    }

    public static ItemStack getBigOre(Material ore) {
        if (!oreMap.containsKey(ore)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED BIG ORE MATERIAL: " + ore.toString());
            throw new IllegalArgumentException();
        }
        ItemStack is = new ItemStack(ore);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(oreMap.get(ore).color + "Big " + oreMap.get(ore).name);
        isMeta.setLocalizedName("big");
        is.setItemMeta(isMeta);
        return is;
    }

    public static ItemStack getMegaOre(Material ore) {
        if (!oreMap.containsKey(ore)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED MEGA ORE MATERIAL: " + ore.toString());
            throw new IllegalArgumentException();
        }
        ItemStack is = new ItemStack(ore);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName("" +  oreMap.get(ore).color + ChatColor.BOLD + "MEGA " + oreMap.get(ore).name.toUpperCase());
        isMeta.addEnchant(Enchantment.DAMAGE_ALL, 0, true);
        isMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        isMeta.setLocalizedName("mega");
        is.setItemMeta(isMeta);
        return is;
    }
}
