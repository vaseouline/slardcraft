package slard.craft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.Damageable;


public class CoatedPickaxe {

    static final String name = "coated_pickaxe";

    static final int diamond_pickaxe_max_durability = 1561;
    static final int coatedPickaxeHealth = 10;

    static String getMetaString(int dmg) {
        return name + ":" + dmg;
    }

    static int getOriginalDamage(ItemStack coatedPickaxe) {
        if (!isCoatedPickaxe(coatedPickaxe)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED ITEM FOR COATED PICKAXE: " + coatedPickaxe.toString());
            throw new IllegalArgumentException();
        }
        coatedPickaxe.getItemMeta().getLocalizedName();
        String[] m = coatedPickaxe.getItemMeta().getLocalizedName().split(":");
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Original Pickaxe Damage: " + Integer.parseInt(m[1]));
        return Integer.parseInt(m[1]);
    }

    static ItemStack getOriginalIronPickaxe(ItemStack coatedPickaxe) {
        if (!isCoatedPickaxe(coatedPickaxe)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED ITEM FOR COATED PICKAXE: " + coatedPickaxe.toString());
            throw new IllegalArgumentException();
        }
        ItemStack ironPickaxe = new ItemStack(Material.IRON_PICKAXE);
        Damageable damageable = (Damageable) ironPickaxe.getItemMeta();
        damageable.setDamage(getOriginalDamage(coatedPickaxe));
        ironPickaxe.setItemMeta(damageable);
        return ironPickaxe;
    }

    static boolean isCoatedPickaxe(ItemStack coatedPickaxe) {
        //I think I have to check if localized but not has meta, i think 
        if (!coatedPickaxe.getItemMeta().hasLocalizedName()) {
            return false;
        }
        return coatedPickaxe.getItemMeta().getLocalizedName().contains(name);
    }

    static String getCoatedPickaxeMetaString(ItemStack ironPickaxe) {
        if (!ironPickaxe.getType().equals(Material.IRON_PICKAXE) || !(ironPickaxe.getItemMeta() instanceof Damageable)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED ITEM FOR IRON PICKAXE: " + ironPickaxe.toString());
            throw new IllegalArgumentException();
        }
        Damageable meta = (Damageable) ironPickaxe.getItemMeta();
        int dmg = meta.getDamage();
        return getMetaString(dmg);
    }

    public static ItemStack getCoatedPickaxe(ItemStack ironPickaxe) {
        if (!ironPickaxe.getType().equals(Material.IRON_PICKAXE)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED ITEM FOR IRON PICKAXE: " + ironPickaxe.toString());
            throw new IllegalArgumentException();
        }
        ItemStack is = new ItemStack(Material.DIAMOND_PICKAXE);
        Damageable isMeta = (Damageable) is.getItemMeta();
        isMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
        isMeta.setDisplayName("" +  ChatColor.AQUA + "Coated " + ChatColor.RESET + "Pickaxe");
        isMeta.setLocalizedName(getCoatedPickaxeMetaString(ironPickaxe));
        
    
        isMeta.setDamage(diamond_pickaxe_max_durability - coatedPickaxeHealth);
        is.setItemMeta(isMeta);
        return is;
    }

    public static Recipe getDiamondCoatedPickaxeRecipe(NamespacedKey nms) {
        ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemStack diamond = new ItemStack(Material.DIAMOND);

        SmithingRecipe recipe = new SmithingRecipe(nms, diamondPickaxe, new MaterialChoice(Material.IRON_PICKAXE), new ExactChoice(diamond));
        return recipe;
    }
}
