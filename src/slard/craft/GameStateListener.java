package slard.craft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GameStateListener implements Listener {

  public static double BORDER_RADIUS = 65;
  static final double OUTSIDE_BORDER_DAMAGE = 3;
  final JavaPlugin plugin;

  BukkitScheduler scheduler = Bukkit.getScheduler();

  public GameStateListener(JavaPlugin plugin) {
    if (System.getenv("BORDER_RADIUS") != null) {
      try {
        BORDER_RADIUS = Integer.parseInt(System.getenv("BORDER_RADIUS"));
      } catch (NumberFormatException e) {
        System.out.println("Border radius could not be set to: " + System.getenv("BORDER_RADIUS"));
      }
      
    }
    this.plugin = plugin;
    setGameRules(getOverWorld(plugin));
    addTownDamage(plugin);
  }

  public static World getOverWorld(JavaPlugin plugin) {
    for (World w : plugin.getServer().getWorlds()) {
      if (w.getEnvironment() == Environment.NORMAL) {
        return w;
      }
    }
    return null;
  }

  public static void setGameRules(World world) {
    world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    world.setGameRule(GameRule.DO_INSOMNIA, false);
    world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 200);
    world.setDifficulty(Difficulty.HARD);
    world.setPVP(true);
    world.setKeepSpawnInMemory(true);
  }

  private static void addTownDamage(JavaPlugin plugin) {
    BukkitScheduler scheduler = Bukkit.getScheduler();
    Location spawn = getOverWorld(plugin).getSpawnLocation();

    scheduler.runTaskTimer(plugin, task -> {
      for (Player p : plugin.getServer().getOnlinePlayers()) {
        if (!inTown(p.getLocation(), spawn) && wearingArmor(p.getInventory())) {
          p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You are taking damage for wearing armor outside the border!"));
          p.damage(OUTSIDE_BORDER_DAMAGE);
        }
      }
    }, 0, 20 * 2 /* 0s, 2s */);
  }

  public static boolean wearingArmor(PlayerInventory inv) {
    return inv.getHelmet() != null || inv.getChestplate() != null || inv.getLeggings() != null
        || inv.getBoots() != null;
  }

  public static boolean inTown(Location loc, Location spawn) {
    if (loc.getWorld().getEnvironment() != Environment.NORMAL) {
      return false;
    }
    double x = loc.getX();
    double z = loc.getZ();

    double[] xz = getBorderXZ(spawn);
    if (x < xz[0] && x > xz[1] && z < xz[2] && z > xz[3]) {
      return true;
    }
    return false;
  }

  public static double[] getBorderXZ(Location spawn) {
    double[] xz = new double[4];
    xz[0] = spawn.getX() + BORDER_RADIUS;
    xz[1] = spawn.getX() - BORDER_RADIUS;
    xz[2] = spawn.getZ() + BORDER_RADIUS;
    xz[3] = spawn.getZ() - BORDER_RADIUS;
    return xz;
  }

  @EventHandler
  public void informPlayerInTown(PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();
    Location spawn = getOverWorld(this.plugin).getSpawnLocation();
    informPlayerInTownHelper(from, to, spawn, event.getPlayer());
  }

  public static void informPlayerInTownHelper(Location from, Location to, Location spawn, Player player) {
    if (inTown(from, spawn) && !inTown(to, spawn)) {
      if (BordSmpPlugin.DEBUG)
        Bukkit.broadcastMessage(player.getDisplayName() + " left town.");
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Left."));
      return;
    }
    if (!inTown(from, spawn) && inTown(to, spawn)) {
      if (BordSmpPlugin.DEBUG)
        Bukkit.broadcastMessage(player.getDisplayName() + " entered town.");
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Returned."));
      return;
    }
  }

  // @EventHandler
  // public void preventBuildOnBorder(BlockPlaceEvent event) {
  //   if (event.getBlock().getLocation().getWorld().getEnvironment() != Environment.NORMAL) {
  //     return;
  //   }
  //   int x = event.getBlock().getX();
  //   int z = event.getBlock().getZ();
  //   Location spawn = getOverWorld(this.plugin).getSpawnLocation();
  //   // weirdness requires negative number to subtract 1 - to match town border
  //   double[] xz = getBorderXZ(spawn);
  //   if (x == xz[0] || x == xz[1] - 1 || z == xz[2] || z == xz[3] - 1) {
  //     event.setCancelled(true);
  //   }
  // }

  @EventHandler
  public void replacePlayerRespawnEvent(PlayerRespawnEvent event) {
    Location spawn = getOverWorld(this.plugin).getSpawnLocation();
    if (!inTown(event.getRespawnLocation(), spawn)) {
      event.setRespawnLocation(spawn);
    }
  }

  @EventHandler
  public void preventOutdoorSleep(PlayerInteractEvent  event) {
    if (event.getClickedBlock() == null) {
      return;
    }
    if (event.getPlayer().getLocation().getWorld().getEnvironment() != Environment.NORMAL) {
      return;
    }
    if (!event.getClickedBlock().getType().toString().contains("BED")) {
      return;
    }
    if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }
    if (event.getPlayer().isSneaking() && event.hasItem()) {
      return;
    }
    if (inTown(event.getPlayer().getLocation(), getOverWorld(this.plugin).getSpawnLocation())) {
      return;
    }
    event.setCancelled(true);
    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
        new TextComponent("Can only sleep within the border."));
  }

  @EventHandler
  public void preventInvDropInTown(PlayerDeathEvent event) {
    // but only if a player killed him
    Player killed = event.getEntity();
    Player killer = event.getEntity().getKiller();
    Location spawn = getOverWorld(this.plugin).getSpawnLocation();
    if (event.getEntity().getKiller() == null) {
      return;
    }
    if (inTown(killed.getLocation(), spawn) || inTown(killer.getLocation(), spawn)) {
      // this could be abused in a fun way.
      // player 1 is nearing town but nearly dead.
      // player 2 shoots a strong arrow at him while in town.
      // player 1 is revived with full hp, keeping exp and inv and is return to town.
      event.setKeepInventory(true);
      event.setKeepLevel(true);
      event.setDroppedExp(0);
      event.getDrops().clear();
    }
  }
}
