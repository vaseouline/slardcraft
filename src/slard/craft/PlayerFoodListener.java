package slard.craft;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import slard.craft.town.GameState;
import slard.craft.town.GameStateListener;


class MeatData {
    String name;

    MeatData(String name) {
        this.name = name;
    }
}

public class PlayerFoodListener implements Listener {

    static final int NORMAL_MEAT_HUNGER = 8;
    static final float NORMAL_MEAT_SATURATION = 12.8f;
    static final int NORMAL_BAKED_POTATO_HUNGER = 5;
    static final float NORMAL_BAKED_POTATO_SATURATION = 6.0f;

    public static Map<Material, MeatData> meatMap = Map.ofEntries(
        entry(Material.COOKED_BEEF, new MeatData("Steak")),
        entry(Material.COOKED_PORKCHOP, new MeatData("Porkchop"))
    );

    public static Set<Material> meatSet = meatMap.keySet();

    @EventHandler
    public void seasonedSteakAndPorkEvent(PlayerItemConsumeEvent event) {
        if (event.getItem().getType().equals(Material.COOKED_BEEF) || event.getItem().getType().equals(Material.COOKED_PORKCHOP)) {
            Material meat = event.getItem().getType();
            ItemStack seasonedVariant = getSeasonedMeat(meat);
            if (event.getItem().isSimilar(seasonedVariant)) {
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER ATE A SEASONED STEAK OR PORKCHOP.");
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION BEFORE: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());
                //buff here
                float newSaturation = event.getPlayer().getSaturation() + NORMAL_MEAT_SATURATION + .5f;
                event.getPlayer().setSaturation(newSaturation);
                int newFoodLevel = event.getPlayer().getFoodLevel() + NORMAL_MEAT_HUNGER + 1;
                event.getPlayer().setFoodLevel(newFoodLevel);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1));
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION AFTER BUMP: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());
                return;
            }
        }
    }

    @EventHandler
    public void eatFancyCookie(PlayerItemConsumeEvent event) {
        if (event.getItem().isSimilar(PlayerFoodListener.getFancyCookie())) {
            //duration is in ticks 22.5 seconds x 20 ticks/s = 450
            //amplifier is 0 for level 1, 1 for level 2
            PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING, 450, 0, true, true);
            event.getPlayer().addPotionEffect(haste);
        }
    }

    @EventHandler
    public void eatButteredPotato(PlayerItemConsumeEvent event) {
        //gives 6 hunger, 7.5 sat
        //absorption 1 for 2 min, regen 2 for 5s
        if (event.getItem().isSimilar((PlayerFoodListener.getButteredPotato()))) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION BEFORE: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());
            float newSaturation = event.getPlayer().getSaturation() + NORMAL_BAKED_POTATO_SATURATION + 1.5f;
            event.getPlayer().setSaturation(newSaturation);
            int newFoodLevel = event.getPlayer().getFoodLevel() + NORMAL_BAKED_POTATO_HUNGER + 1;
            event.getPlayer().setFoodLevel(newFoodLevel);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120 * 20, 1));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 *  20, 2));
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("PLAYER HUNGER/SATURATION AFTER BUMP: " + event.getPlayer().getFoodLevel() + "/" + event.getPlayer().getSaturation());

            if (!GameState.getPlayerState(event.getPlayer()).isButtered) {
                if (makeButtered(event.getPlayer())) {
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD+"Buttered"));
                }
            }
        }
    }

    public static boolean makeButtered(Player player) {
        if (isWearingButterOutfit(player)) {
            GameState.getPlayerState(player).isButtered = true;
            PlayerInventory inv = player.getInventory();
            ItemMeta butterHat = inv.getHelmet().getItemMeta();
            butterHat.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            butterHat.setUnbreakable(true);
            butterHat.setDisplayName(ChatColor.GOLD + "Butter");
            List<String> lore = new ArrayList<String>();
            lore.add("A helmet slathered in butter.");
            lore.add("Other armor pieces seem to");
            lore.add("slip off because of the butter.");
            butterHat.setLore(lore);
            inv.getHelmet().setItemMeta(butterHat);

            ItemStack air = new ItemStack(Material.BARRIER);
            ItemMeta blockArmor = air.getItemMeta();
            blockArmor.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            blockArmor.setDisplayName(ChatColor.RED + "Too slippery");
            air.setItemMeta(blockArmor);
            inv.setBoots(air.clone());
            inv.setChestplate(air.clone());
            inv.setLeggings(air.clone());
            return true;
        }
        return false;
    }

    public static boolean isWearingButterOutfit(Player player) {

        PlayerInventory inv = player.getInventory();
    
        if (inv.getHelmet() != null && inv.getHelmet().isSimilar(new ItemStack(Material.GOLDEN_HELMET))) {
            if (inv.getChestplate() == null && inv.getLeggings() == null && inv.getBoots() == null) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack getSeasonedMeat(Material meat) {
        if (!meatMap.containsKey(meat)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("ERROR: UNEXPECTED SEASONED MEAT MATERIAL: " + meat.toString());
            throw new IllegalArgumentException();
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
            throw new IllegalArgumentException();
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

    public static ItemStack getButteredPotato() {
        ItemStack is = new ItemStack(Material.BAKED_POTATO);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Buttered Potato");
        isMeta.setLocalizedName("buttered_potato");
        is.setItemMeta(isMeta);
        return is;
    }

    public static Recipe getButteredPotatoRecipe(NamespacedKey nms) {
        ItemStack output = getButteredPotato();
        ShapedRecipe recipe = new ShapedRecipe(nms, output);
        recipe.setGroup("pG");
        recipe.shape("G", "p");
        recipe.setIngredient('p', Material.BAKED_POTATO);
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        return recipe;
    }
}
