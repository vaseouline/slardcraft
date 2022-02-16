package slard.craft.town;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import slard.craft.SlardcraftPlugin;

public class GameStateListener implements Listener {

  static final double BORDER_RADIUS = 65;

  private final SlardcraftPlugin plugin;
  BukkitScheduler scheduler = Bukkit.getScheduler();

  public GameStateListener(SlardcraftPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void putGameState(PlayerJoinEvent event) {
    
    if (GameState.getPlayerState(event.getPlayer()) == null) {
      Location loc = event.getPlayer().getLocation();
      if (!GameState.players.containsKey(event.getPlayer().getUniqueId())) {
        PlayerState ps = new PlayerState(false, inTown(loc));
        GameState.players.put(event.getPlayer().getUniqueId(), ps);
        if (SlardcraftPlugin.DEBUG) {
          Bukkit.broadcastMessage(event.getPlayer().getDisplayName() + " added to gamestate.");
        }
      }
      
      
    }
  }

  @EventHandler
  public void checkPlayerWildies(PlayerMoveEvent event) {

    PlayerState ps = GameState.getPlayerState(event.getPlayer());
    Location tloc = event.getTo();
    if (ps.inTown && !inTown(tloc)) {
      if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Player move Left town.");
      event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Left."));
    }
    if (!ps.inTown && inTown(tloc)) {
      if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Player move Enter town.");
      event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Returned."));
    }    
    ps.inTown = inTown(tloc);

    if (!inTown(tloc) && !ps.hasDmgTask && !ps.isButtered) {
      scheduler.runTaskTimer(plugin, task -> {
        if (ps.inTown || ps.isButtered) {
          task.cancel();
          ps.hasDmgTask = false;
        }
        event.getPlayer().damage(3);
      }, 20 * 1, 20 * 2 /* 1s 2s */);
      ps.hasDmgTask = true;
    }
    
  }


  @EventHandler
  public void playerRespawnEvent(PlayerRespawnEvent event) {
    PlayerState ps = GameState.getPlayerState(event.getPlayer());
    if (inTown(event.getRespawnLocation())) {
      event.getPlayer().setNoDamageTicks(20 * 2);
      ps.inTown = true;
    }
  }

  @EventHandler
  public void playerTeleportEvent(PlayerTeleportEvent event) {
    PlayerState ps = GameState.getPlayerState(event.getPlayer());
    if (inTown(event.getTo())) {
      event.getPlayer().setNoDamageTicks(20 * 2);
      ps.inTown = true;
    }
  }

  @EventHandler
  public void playerPortalEvent(PlayerPortalEvent event) {
    PlayerState ps = GameState.getPlayerState(event.getPlayer());
    if (inTown(event.getTo())) {
      event.getPlayer().setNoDamageTicks(20 * 2);
      ps.inTown = true;
    }
  }

  public boolean inTown(Location loc) {
    double x = loc.getX();
    double z = loc.getZ();

    if (x < BORDER_RADIUS && x > BORDER_RADIUS * -1 && z < BORDER_RADIUS && z > BORDER_RADIUS*-1) {
      return true;
    }
    return false;
  }

  @EventHandler
  public void preventBuildOnBorder(BlockPlaceEvent event) {
    if (!GameState.getPlayerState(event.getPlayer()).isButtered) {
      return;
    }
    int x = event.getBlock().getX();
    int z = event.getBlock().getZ();
    // weirdness requires negative number to subtract 1
    if (x == BORDER_RADIUS || x == BORDER_RADIUS*-1 - 1 || z == BORDER_RADIUS || z == BORDER_RADIUS*-1 - 1) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void preventOutsideBuildAsTownie(BlockPlaceEvent event) {
    if (GameState.getPlayerState(event.getPlayer()).isButtered) {
      return;
    }
    int x = event.getBlock().getX();
    int z = event.getBlock().getZ();
    
    // weirdness requires negative number to subtract 1
    if (x >= BORDER_RADIUS || x <= BORDER_RADIUS*-1 - 1 || z >= BORDER_RADIUS || z <= BORDER_RADIUS*-1 - 1) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void preventOutdoorSleep(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }
    if (event.getPlayer().getLocation().getWorld().getEnvironment() != Environment.NORMAL) {
      return;
    }
    if (!event.getClickedBlock().getType().toString().contains("BED")) {
      return;
    }
    if (!inTown(event.getPlayer().getLocation())) {
      event.setCancelled(true);
      event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Can only sleep within the border."));
    }
  }

  @EventHandler
  public void preventTowniePVP(EntityDamageByEntityEvent event) {
    Entity damager = event.getDamager();
    Entity damagee = event.getEntity();
    if (!damager.getType().equals(EntityType.PLAYER)) {
      return;
    }
    if (!damagee.getType().equals(EntityType.PLAYER)) {
      return;
    }
    PlayerState pDamager = GameState.getPlayerState((Player) damager);
    PlayerState pDamagee = GameState.getPlayerState((Player) damagee);
    if (!pDamager.isButtered || !pDamagee.isButtered) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void preventInvDrop(PlayerDeathEvent event) {
    if (GameState.getPlayerState(event.getEntity()).isButtered) {
      event.setKeepInventory(true);
      Map<Integer, ? extends ItemStack> tridents = event.getEntity().getInventory().all(Material.TRIDENT);
      event.getDrops().clear();
      boolean droppedTrident = false;
      for (ItemStack is : tridents.values()) {
        if (is.containsEnchantment(Enchantment.BINDING_CURSE)) {
          continue;
        }
        event.getDrops().add(is);
        droppedTrident = true;
      }
      for (Integer i : tridents.keySet()) {
        event.getEntity().getInventory().clear(i);
      }
      if (droppedTrident) {
        event.setDeathMessage(event.getDeathMessage() + " and dropped a trident.");
      }
      
    }
    
  }

}

