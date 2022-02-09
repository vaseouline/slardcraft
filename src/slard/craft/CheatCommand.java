package slard.craft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import slard.craft.town.GameState;

public class CheatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (arg0 instanceof Player) {
            Player player = (Player) arg0;

            // Give the player our items (comma-seperated list of all ItemStack)
            player.getInventory().addItem(
                    PlayerFoodListener.getSeasonedMeat(Material.COOKED_BEEF),
                    PlayerFoodListener.getSeasonedMeat(Material.COOKED_PORKCHOP),
                    PlayerFoodListener.getFancyCookie(),
                    PlayerFoodListener.getFancySugar()
                    );
            player.setHealth(10);
            player.setFoodLevel(1);
            player.setSaturation(1);
            Bukkit.broadcastMessage(GameState.toStringPlayerStates());
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }

}
