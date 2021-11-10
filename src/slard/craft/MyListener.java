package slard.craft;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.Map.entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class MyListener implements Listener {
    
    private static List<Material> BANNED_ENCHANT_LIST = Arrays.asList(
        Material.DIAMOND_AXE, 
        Material.DIAMOND_BOOTS, 
        Material.DIAMOND_CHESTPLATE,
        Material.DIAMOND_HELMET,
        Material.DIAMOND_HOE,
        Material.DIAMOND_LEGGINGS,
        Material.DIAMOND_PICKAXE,
        Material.DIAMOND_SHOVEL,
        Material.DIAMOND_SWORD,
        Material.NETHERITE_AXE, 
        Material.NETHERITE_BOOTS, 
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_HELMET,
        Material.NETHERITE_HOE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_PICKAXE,
        Material.NETHERITE_SHOVEL,
        Material.NETHERITE_SWORD,
        Material.ELYTRA,
        Material.STONE_AXE, 
        Material.STONE_HOE,
        Material.STONE_PICKAXE,
        Material.STONE_SHOVEL,
        Material.STONE_SWORD,
        Material.GOLDEN_AXE, 
        Material.GOLDEN_BOOTS, 
        Material.GOLDEN_CHESTPLATE,
        Material.GOLDEN_HELMET,
        Material.GOLDEN_HOE,
        Material.GOLDEN_LEGGINGS,
        Material.GOLDEN_PICKAXE,
        Material.GOLDEN_SHOVEL,
        Material.GOLDEN_SWORD,
        Material.IRON_AXE, 
        Material.IRON_BOOTS, 
        Material.IRON_CHESTPLATE,
        Material.IRON_HELMET,
        Material.IRON_HOE,
        Material.IRON_LEGGINGS,
        Material.IRON_PICKAXE,
        Material.IRON_SHOVEL,
        Material.IRON_SWORD,
        Material.CHAINMAIL_BOOTS,
        Material.CHAINMAIL_CHESTPLATE,
        Material.CHAINMAIL_HELMET,
        Material.CHAINMAIL_LEGGINGS,
        Material.SHIELD,
        Material.BOW,
        Material.CROSSBOW,
        Material.TRIDENT
    );
    private static Set<Material> BANNED_ENCHANT_SET = new HashSet<>(BANNED_ENCHANT_LIST);
    

    @EventHandler
    public void disableIronGolemIronDrop(EntityDeathEvent event) {
        if (event.getEntity() instanceof IronGolem) {
            IronGolem ig = (IronGolem) event.getEntity();
            if (!ig.isPlayerCreated()) {
                Iterator<ItemStack> iterator = event.getDrops().iterator();
                while(iterator.hasNext())
                {
                    ItemStack item = iterator.next();
                    if(item.getType().equals(Material.IRON_INGOT))
                    {
                        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("DESTROYED IRON DROP FROM IRON GOLEM");
                        iterator.remove();  
                    }
                }
            }
        }
    }


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

    @EventHandler
    public void sanitizeEnchantments(PrepareItemEnchantEvent event) {
        if (BANNED_ENCHANT_SET.contains(event.getItem().getType())) {
            EnchantmentOffer[] eo = event.getOffers();
            for (int i = 0; i < eo.length; i++) {
                eo[i] = null;
            }
        }
    }

    @EventHandler
    public void sanitizeAnvil(PrepareAnvilEvent event) {
        //TODO should set this to allow repairs of vanilla stuff. Currently, built this way to prevent enchantments with book.
        if (BANNED_ENCHANT_SET.contains(event.getResult().getType())) {
            event.setResult(null);
            return;
        }

        //prevent renaming of custom items
        if (BigOre.oreSet.contains(event.getResult().getType())) {
            if (event.getResult().getItemMeta().getLocalizedName().equals("mega") || event.getResult().getItemMeta().getLocalizedName().equals("big")) {
                event.setResult(null);
                return;
            }
        }
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

    @EventHandler
    public void bigYieldOres(BlockDropItemEvent event) {
        Map<Material, Material> BIG_YIELD_ORE_MAP = Map.ofEntries(
            entry(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND),
            entry(Material.DIAMOND_ORE, Material.DIAMOND),
            entry(Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD),
            entry(Material.GOLD_ORE, Material.RAW_GOLD),
            entry(Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON),
            entry(Material.IRON_ORE, Material.RAW_IRON)
            
        );
        Set<Material> BIG_YIELD_ORE_SET = BIG_YIELD_ORE_MAP.keySet();
        BlockState bs = event.getBlockState();
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Block state type: " + bs.getType());
        if (BIG_YIELD_ORE_SET.contains(bs.getType())) {
            Random rand = new Random();
            List<Item> items = event.getItems();
            for(Item i : items) {
                float r = rand.nextFloat();
                switch(normalBigOrMega(r)) {
                    case 0:
                    //normal
                        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("NORMAL");
                        break;
                    case 1:
                    //big
                        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("BEEG");
                        ItemStack bigOre = BigOre.getBigOre(BIG_YIELD_ORE_MAP.get(bs.getType()));
                        i.setItemStack(bigOre);
                        break;
                    case 2:
                    //mega
                        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("MEGA");
                        if (bs.getType().equals(Material.DIAMOND_ORE) || bs.getType().equals(Material.DEEPSLATE_DIAMOND_ORE)) {
                            Bukkit.broadcastMessage("" + event.getPlayer().getName() + " just mined a " + BigOre.oreMap.get(Material.DIAMOND).color + ChatColor.BOLD + "MEGA DIAMOND" + ChatColor.RESET + "!");
                            //play sound for all players
                        }
                        ItemStack megaOre = BigOre.getMegaOre(BIG_YIELD_ORE_MAP.get(bs.getType()));
                        i.setItemStack(megaOre);
                        break;
                }
            }
        }
    }


    private int normalBigOrMega(float chance) {
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("FLOAT CHANCE: " + chance);
        if (chance <= .01) {
            //1% chance of mega
            return 2;
        }
        if (chance <= .11) {
            //10% chance of big
            return 1;
        }
        return 0;
    }

    @EventHandler
    public void sanitizeRepair(PrepareItemCraftEvent event) {
        if (!event.isRepair()) {
            return;
        }
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Repair Event item: " + event.toString());
        CraftingInventory ci = event.getInventory();
        if (SlardcraftPlugin.BANNED_CRAFT_SET.contains(ci.getItem(0).getType())) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Repair Event item DENIED: " + ci.getItem(0).getType());
            ci.setResult(null);
        }
    }

    public static boolean sanitizeItemStack(ItemStack is) {
        if (is == null || isException(is)) {
            return false;
        }
        boolean sanitized = false;
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Original item: " + is.toString());
        if (BANNED_ENCHANT_SET.contains(is.getType())) {
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
        if (isMending(is)) {
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("replacing mending book with legal item: book");
            sanitizeEnchantedBook(is);
            sanitized = true;
        }
        return sanitized;
    }

    private static boolean isMending(ItemStack is) {
        if (is.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) is.getItemMeta();
            if (esm.hasStoredEnchant(Enchantment.MENDING)) {
                return true;
            }
        }
        return false;
    }

    private static void sanitizeEnchantedBook(ItemStack is) {
        is.setType(Material.BOOK);
        ItemStack book = new ItemStack(Material.BOOK);
        is.setItemMeta(book.getItemMeta());
    }

    private static boolean isException(ItemStack is) {
        if (CoatedPickaxe.isCoatedPickaxe(is)) {
            return true;
        }
        return false;
    }

}