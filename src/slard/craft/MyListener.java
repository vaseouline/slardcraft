package slard.craft;

import static java.util.Map.entry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class MyListener implements Listener {
    
    private static List<Material> BANNED_ENCHANTED_MELEE_LIST = Arrays.asList(
        Material.DIAMOND_AXE, 
        Material.DIAMOND_SWORD,
        Material.NETHERITE_AXE, 
        Material.NETHERITE_SWORD,
        Material.STONE_AXE, 
        Material.STONE_SWORD,
        Material.GOLDEN_AXE, 
        Material.GOLDEN_SWORD,
        Material.IRON_AXE, 
        Material.IRON_SWORD

    );
    private static Set<Material> BANNED_ENCHANTED_MELEE_SET = new HashSet<>(BANNED_ENCHANTED_MELEE_LIST);


    private static List<Material> PICKAXES_LIST = Arrays.asList(
        Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE,
        Material.STONE_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.IRON_PICKAXE
    );
    private static Set<Material> PICKAXES_SET = new HashSet<>(PICKAXES_LIST);

    private static List<Material> MISC_LIST = Arrays.asList(
    Material.SHIELD,
    Material.BOW,
    Material.CROSSBOW,
    Material.TRIDENT,
    Material.ELYTRA
    );


    private static List<Material> GOLD_NUGGETABLE_LIST = Arrays.asList(
        Material.GOLD_NUGGET,
        Material.GOLD_INGOT,
        Material.GOLDEN_AXE, 
        Material.GOLDEN_BOOTS, 
        Material.GOLDEN_CHESTPLATE,
        Material.GOLDEN_HELMET,
        Material.GOLDEN_HOE,
        Material.GOLDEN_LEGGINGS,
        Material.GOLDEN_PICKAXE,
        Material.GOLDEN_SHOVEL,
        Material.GOLDEN_SWORD
    );
    private static Set<Material> GOLD_NUGGETABLE_SET = new HashSet<>(GOLD_NUGGETABLE_LIST);
    @EventHandler
    public void disableZombiePiglinGoldDrop(EntityDeathEvent event) {
        if (event.getEntity() instanceof PigZombie || event.getEntity() instanceof Piglin) {            
            Iterator<ItemStack> iterator = event.getDrops().iterator();
            while(iterator.hasNext())
            {
                ItemStack item = iterator.next();
                if(GOLD_NUGGETABLE_SET.contains(item.getType()))
                {
                    if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("DESTROYED " + item.getType().toString() + " FROM " + event.getEntityType().toString());
                    iterator.remove();  
                }
            }
            
        }
    }

    @EventHandler
    public void sanitizePiglinBarter(PiglinBarterEvent event) {
        List<ItemStack> isList = event.getOutcome();
        for (ItemStack is : isList) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Sanitizing barter from piglin.");
            sanitizeItemStack(is);
        }
    }

    // TODO for pickaxes first, then do it on anvil as well, and sanitize item stack
    @EventHandler
    public void sanitizeEnchantmentTable(PrepareItemEnchantEvent event) {
        // Makes enchantments dissappear for melee weapons
        if (BANNED_ENCHANTED_MELEE_SET.contains(event.getItem().getType())) {
            EnchantmentOffer[] eo = event.getOffers();
            for (int i = 0; i < eo.length; i++) {
                eo[i] = null;
            }
            return;
        }
        if (PICKAXES_SET.contains(event.getItem().getType())) {
            EnchantmentOffer[] eo = event.getOffers();
            for (int i = 0; i < eo.length; i++) {
                if (eo[i].getEnchantment() == Enchantment.LOOT_BONUS_BLOCKS) {
                    eo[i] = null;
                }
            }
            return;
        }
    }

    @EventHandler
    public void sanitizeEnchantmentCompletion(EnchantItemEvent event) {
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        enchants.remove(Enchantment.LOOT_BONUS_BLOCKS);
    }

    @EventHandler
    public void sanitizeAnvil(PrepareAnvilEvent event) {
        if (BANNED_ENCHANTED_MELEE_SET.contains(event.getResult().getType())) {
            event.setResult(null);
            return;
        }

        //prevent renaming of custom items
        if (PlayerFoodListener.meatSet.contains((event.getResult().getType()))) {
            if (event.getResult().getItemMeta().getLocalizedName().equals("seasoned")) {
                event.setResult(null);
                return;
            }
        }
        if (event.getResult().getType().equals(Material.SUGAR)) {
            if (event.getResult().getItemMeta().getLocalizedName().equals("fancy_sugar")) {
                event.setResult(null);
                return;
            }
        }
        if (event.getResult().getType().equals(Material.COOKIE)) {
            if (event.getResult().getItemMeta().getLocalizedName().equals("fancy_cookie")) {
                event.setResult(null);
                return;
            }
        }
    }

    
    @EventHandler
    public void sanitizeVillager(VillagerAcquireTradeEvent event) {
        
        MerchantRecipe mr = event.getRecipe();
        //first santize all enchantments if in list
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

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void sanitizeMobLoot(EntityDeathEvent event) {
        List<ItemStack> loot = event.getDrops();
        for (int i = 0; i < loot.size(); i++) {
            ItemStack is = loot.get(i);
            if (sanitizeItemStack(is)) {
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Sanitized loot for: " + event.getEventName());
            }
            loot.set(i, is);
        }
        
    }

    @EventHandler
    public void sanitizeInventoryOpenEvent(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        Iterator<ItemStack> ii = inv.iterator();
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Attempting Sanitization on inventory.");
        while(ii.hasNext()) {
            ItemStack is = ii.next();
            sanitizeItemStack(is);
        }
    }

    //Hypothetically, not neccessary if everything else is cleaned up. a catch all for pickups. Maybe could use a catch all for inventory dragins,.. no such thing kind of.
    @EventHandler
    public void sanitizePlayerPickups(EntityPickupItemEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) {
            return;
        }
        Item item = event.getItem();
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Attempting Sanitization on: " + event.toString());
        sanitizeItemStack(item.getItemStack());
    }

    public static boolean sanitizeItemStack(ItemStack is) {
        if (is == null || isException(is)) {
            return false;
        }
        boolean sanitized = false;
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Original item: " + is.toString());
        if (BANNED_ENCHANTED_MELEE_SET.contains(is.getType())) {
            for(Enchantment e : is.getEnchantments().keySet()){
                is.removeEnchantment(e);
                if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("removing enchants from item: " + e.toString());
                sanitized = true;
            }
        }
        if (SlardcraftPlugin.BANNED_CRAFT_SET.contains(is.getType())) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("replacing banned with legal item: " + SlardcraftPlugin.BANNED_CRAFT_MAP.get(is.getType()).toString());
            is.setType(SlardcraftPlugin.BANNED_CRAFT_MAP.get(is.getType()));
            sanitized = true;
        }
        return sanitized;
    }

    private static boolean isException(ItemStack is) {
        return false;
    }

}