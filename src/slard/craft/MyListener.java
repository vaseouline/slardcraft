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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

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
        Material.CROSSBOW
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
                        iterator.remove();  
                    }
                }
            }
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
        if (BANNED_ENCHANT_SET.contains(event.getResult().getType())) {
            event.setResult(null);
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
    public void sanitizeInventoryEvent(InventoryOpenEvent event) {
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
        Item item = event.getItem();
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Attempting Sanitization on: " + event.toString());
        sanitizeItemStack(item.getItemStack());
    }   

    @EventHandler
    public void bigYieldOres(BlockDropItemEvent event) {
        // if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Block drop item: " + event.getItems());
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
            // if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Block drop item MATCH.");
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
                        ItemStack bigOre = new ItemStack(BIG_YIELD_ORE_MAP.get(bs.getType()), 3);
                        i.setItemStack(bigOre);
                        break;
                    case 2:
                    //mega
                        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("MEGA");
                        if (bs.getType().equals(Material.DIAMOND)) Bukkit.broadcastMessage("" + event.getPlayer().getName() + " just got a MEGA DIAMOND.");
                        ItemStack megaOre = new ItemStack(BIG_YIELD_ORE_MAP.get(bs.getType()), 9);
                        i.setItemStack(megaOre);
                        break;
                }
            }
        }
    }


    private int normalBigOrMega(float chance) {
        if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("FLOAT CHANCE: " + chance);
        if (chance <= .01) {
            return 2;
        }
        if (chance <= .11) {
            return 1;
        }
        return 0;
    }

    public static boolean sanitizeItemStack(ItemStack is) {
        if (is == null) {
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
        return sanitized;
    }

}