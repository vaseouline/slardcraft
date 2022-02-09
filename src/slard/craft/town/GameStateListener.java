package slard.craft.town;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import slard.craft.SlardcraftPlugin;

public class GameStateListener implements Listener {

  static final double WORLD_RADIUS = 65;

  private final SlardcraftPlugin plugin;
  BukkitScheduler scheduler = Bukkit.getScheduler();

  public GameStateListener(SlardcraftPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void putGameState(PlayerJoinEvent event) {
    
    if (GameState.players.get(event.getPlayer().getUniqueId()) == null) {
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

    PlayerState ps = GameState.players.get(event.getPlayer().getUniqueId());
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

    if (!inTown(tloc) && !ps.hasDmgTask) {
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
    PlayerState ps = GameState.players.get(event.getPlayer().getUniqueId());
    event.getPlayer().setNoDamageTicks(20);
    ps.inTown = true;
  }

  public boolean inTown(Location loc) {
    double x = loc.getX();
    double z = loc.getZ();

    if (loc.getWorld().getEnvironment() == Environment.NETHER) {
      if (x < WORLD_RADIUS/8 && x > WORLD_RADIUS/8 * -1 && z < WORLD_RADIUS/8 && z > WORLD_RADIUS/8*-1) {
        return true;
      }
      return false;
    }
    if (x < WORLD_RADIUS && x > WORLD_RADIUS * -1 && z < WORLD_RADIUS && z > WORLD_RADIUS*-1) {
      return true;
    }
    return false;
  }

  @EventHandler
  public void preventBuildOnBorder(BlockPlaceEvent event) {
    int x = event.getBlock().getX();
    int z = event.getBlock().getZ();
    // weirdness requires negative number to subtract 2
    if (x == WORLD_RADIUS + 1 || x == WORLD_RADIUS*-1 - 2 || z == WORLD_RADIUS + 1 || z == WORLD_RADIUS*-1 - 2) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void preventOutdoorSleep(PlayerInteractEvent event) {
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


}

