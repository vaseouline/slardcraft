package slard.craft;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class DisableEnchantListener implements Listener {
    private static List<Material> PICKAXES_LIST = Arrays.asList(
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE,
            Material.STONE_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE);
    private static Set<Material> PICKAXES_SET = new HashSet<>(PICKAXES_LIST);

    @EventHandler
    public void sanitizeEnchantmentCompletion(EnchantItemEvent event) {

        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();

        if (PICKAXES_SET.contains(event.getItem().getType())) {
            if (enchants.remove(Enchantment.LOOT_BONUS_BLOCKS) != null) {
                enchants.put(Enchantment.MENDING, 1);
                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("Replaced fortune with mending");
                return;
            }
        }
        if (event.getItem().getType().equals(Material.BOW)) {
            if (enchants.remove(Enchantment.ARROW_INFINITE) != null) {
                enchants.put(Enchantment.MENDING, 1);
                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("Replaced arrow infinite with mending");
                return;
            }
        }
        if (event.getItem().getType().equals(Material.BOOK)) {
            if (enchants.remove(Enchantment.ARROW_INFINITE) != null) {
                enchants.put(Enchantment.DURABILITY, 1);
                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("Replaced arrow infinite with durability");
                return;
            }
        }
    }

    @EventHandler
    public void sanitizeAnvil(PrepareAnvilEvent event) {
        if (PICKAXES_SET.contains(event.getResult().getType())) {
            AnvilInventory inv = event.getInventory();
            ItemStack is = inv.getItem(1);
            if (isEnchantedBook(is, Enchantment.LOOT_BONUS_BLOCKS)) {
                event.setResult(null);
                return;
            }
        }

        if (event.getResult().getType().equals(Material.BOW)) {
            AnvilInventory inv = event.getInventory();
            ItemStack is = inv.getItem(1);
            if (isEnchantedBook(is, Enchantment.ARROW_INFINITE)) {
                event.setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void sanitizeVillager(VillagerAcquireTradeEvent event) {

        MerchantRecipe mr = event.getRecipe();
        ItemStack result = mr.getResult();
        if (sanitizeItemStack(result)) {
            MerchantRecipe nmr = new MerchantRecipe(result, mr.getMaxUses());
            nmr.setExperienceReward(true);
            nmr.setIngredients(mr.getIngredients());
            nmr.setPriceMultiplier(mr.getPriceMultiplier());
            nmr.setVillagerExperience(mr.getVillagerExperience());
            event.setRecipe(nmr);
        }

    }

    @EventHandler
    public void sanitizeInventoryOpenEvent(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        Iterator<ItemStack> ii = inv.iterator();
        if (SlardcraftPlugin.DEBUG)
            Bukkit.broadcastMessage("Attempting Sanitization on inventory.");
        while (ii.hasNext()) {
            ItemStack is = ii.next();
            sanitizeItemStack(is);
        }
    }

    @EventHandler
    public void sanitizePlayerPickups(EntityPickupItemEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) {
            return;
        }
        Item item = event.getItem();
        if (SlardcraftPlugin.DEBUG)
            Bukkit.broadcastMessage("Attempting Sanitization on: " + event.toString());
        sanitizeItemStack(item.getItemStack());
    }

    public static boolean sanitizeItemStack(ItemStack is) {
        if (is == null) {
            return false;
        }

        if (SlardcraftPlugin.DEBUG)
            Bukkit.broadcastMessage("Original item: " + is.toString());

        if (SlardcraftPlugin.BANNED_CRAFT_SET.contains(is.getType())) {
            if (SlardcraftPlugin.DEBUG)
                Bukkit.broadcastMessage("replacing banned with legal item: "
                        + SlardcraftPlugin.BANNED_CRAFT_MAP.get(is.getType()).toString());
            is.setType(SlardcraftPlugin.BANNED_CRAFT_MAP.get(is.getType()));
            return true;
        }

        if (is.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta eSM = (EnchantmentStorageMeta) is.getItemMeta();
            if (eSM.hasStoredEnchant(Enchantment.ARROW_INFINITE)) {
                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("replacing " + "infinity" + " with book: " + is.toString());
                is.setType(Material.BOOK);
                return true;
            }
        }

        if (is.getType().equals(Material.BOW)) {
            int removed = is.removeEnchantment(Enchantment.ARROW_INFINITE);
            if (removed > 0) {
                is.addEnchantment(Enchantment.MENDING, 1);

                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("removing " + "infinity" + " from item: " + is.toString());

                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("adding " + "mending" + " to item: " + is.toString());

                return true;
            }
        }
        if (PICKAXES_SET.contains(is.getType())) {
            int removed = is.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
            if (removed > 0) {
                is.addEnchantment(Enchantment.MENDING, 1);

                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("removing " + "fortune" + " from item: " + is.toString());

                if (SlardcraftPlugin.DEBUG)
                    Bukkit.broadcastMessage("adding " + "mending" + " to item: " + is.toString());

                return true;
            }
        }

        return false;
    }

    private static boolean isEnchantedBook(ItemStack is, Enchantment enchantment) {
        if (is == null) {
            return false;
        }
        if (is.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) is.getItemMeta();
            if (esm.hasStoredEnchant(enchantment)) {
                return true;
            }
        }
        return false;
    }
}
