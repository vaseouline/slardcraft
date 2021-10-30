package slard.craft;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

class MeatData {
    String name;

    MeatData(String name) {
        this.name = name;
    }
}

public class PlayerFoodListener implements Listener {

    static final int NERFED_HUNGER = 6;
    static final float NERFED_SATURATION = 8;
    static final int NORMAL_HUNGER = 8;
    static final float NORMAL_SATURATION = 12.8f;

    public static Map<Material, MeatData> meatMap = Map.ofEntries(
        entry(Material.COOKED_BEEF, new MeatData("Steak")),
        entry(Material.COOKED_PORKCHOP, new MeatData("Porkchop"))
    );

    public static Set<Material> meatSet = meatMap.keySet();

    @EventHandler
    public void nerfSteakAndPork(PlayerItemConsumeEvent event) {
        if (event.getItem().getType().equals(Material.COOKED_BEEF) || event.getItem().getType().equals(Material.COOKED_PORKCHOP)) {
            Material meat = event.getItem().getType();
            ItemStack seasonedVariant = getSeasonedMeat(meat);
            if (!event.getItem().isSimilar(seasonedVariant)) {
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER ATE A NORMAL STEAK OR PORKCHOP.");
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION BEFORE: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());
                //nerf here
                float newSaturation = event.getPlayer().getSaturation() - (NORMAL_SATURATION - NERFED_SATURATION);
                event.getPlayer().setSaturation(newSaturation);
                int newFoodLevel = event.getPlayer().getFoodLevel() - (NORMAL_HUNGER - NERFED_HUNGER);
                event.getPlayer().setFoodLevel(newFoodLevel);
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION AFTER DOWN BUMP: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());
                return;
            }
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER ATE A SEASONED STEAK OR PORKCHOP.");
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION BEFORE: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());
        }
    }

    @EventHandler
    public void eatFancyCookie(PlayerItemConsumeEvent event) {
        if (event.getItem().isSimilar(PlayerFoodListener.getFancyCookie())) {
            //duration is in ticks 22.5 seconds x 20 ticks/s = 450
            PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING, 450, 1, true, true);
            event.getPlayer().addPotionEffect(haste);
        }
    }

    public static ItemStack getSeasonedMeat(Material meat) {
        if (!meatMap.containsKey(meat)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED SEASONED MEAT MATERIAL: " + meat.toString());
        }
        ItemStack is = new ItemStack(meat);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.RED + "Seasoned " + meatMap.get(meat).name);
        isMeta.setLocalizedName("seasoned");
        is.setItemMeta(isMeta);
        return is;
    }

    public static Recipe getSeasonedMeatRecipe(NamespacedKey nms, Material meat) {
        if (!meatMap.containsKey(meat)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED SEASONED MEAT MATERIAL: " + meat.toString());
        }
        ItemStack output = getSeasonedMeat(meat);
        ShapedRecipe recipe = new ShapedRecipe(nms, output);
        recipe.setGroup("Mg");
        recipe.shape("ggg", "gMg", "ggg");
        recipe.setIngredient('M', new ExactChoice(new ItemStack(meat)));
        recipe.setIngredient('g', Material.GOLD_NUGGET);
        return recipe;
    }

    public static ItemStack getFancySugar() {
        ItemStack is = new ItemStack(Material.SUGAR);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Fancy Sugar");
        isMeta.setLocalizedName("fancy_sugar");
        is.setItemMeta(isMeta);
        return is;
    }

    public static Recipe getFancySugarRecipe(NamespacedKey nms) {
        ItemStack output = getFancySugar();
        output.setAmount(4);
        ShapedRecipe recipe = new ShapedRecipe(nms, output);
        recipe.setGroup("ds");
        recipe.shape("ss", "ds");
        recipe.setIngredient('d', new ExactChoice(new ItemStack(Material.DIAMOND)));
        recipe.setIngredient('s', new ExactChoice(new ItemStack(Material.SUGAR)));
        return recipe;
    }

    public static ItemStack getFancyCookie() {
        ItemStack is = new ItemStack(Material.COOKIE);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Fancy Cookie");
        isMeta.setLocalizedName("fancy_cookie");
        is.setItemMeta(isMeta);
        return is;
    }

    public static Recipe getFancyCookieRecipe(NamespacedKey nms) {
        ItemStack output = getFancyCookie();
        output.setAmount(8);
        ShapedRecipe recipe = new ShapedRecipe(nms, output);
        recipe.setGroup("wfc*");
        recipe.shape("*f*", "wcw");
        recipe.setIngredient('w', Material.WHEAT);
        recipe.setIngredient('f', new ExactChoice(PlayerFoodListener.getFancySugar()));
        recipe.setIngredient('c', Material.COCOA_BEANS);
        recipe.setIngredient('*', Material.AIR);
        return recipe;
    }
}
