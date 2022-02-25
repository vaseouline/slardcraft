package slard.craft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;


public class InBorderCommand implements CommandExecutor {

    JavaPlugin plugin;
    
    InBorderCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0) {
            sender.sendMessage("/inborder needs exactly 0 args.");
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be a player to use this command.");
        }
        Player player = (Player) sender;
        if (GameStateListener.inTown(player.getLocation(), GameStateListener.getOverWorld(plugin).getSpawnLocation())) {
            sender.sendMessage(ChatColor.GREEN + "You are within the border.");
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "You are outside the border.");
            return true;
        }
    }

}
