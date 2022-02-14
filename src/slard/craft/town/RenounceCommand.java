package slard.craft.town;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class RenounceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (arg0 instanceof Player) {
            Player player = (Player) arg0;
            PlayerState ps = GameState.getPlayerState(player);
            if (ps == null) {
                System.out.println("player tried renouncing while their playerstate was null: " + player.getUniqueId());
            }
            if (!ps.inTown) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Can only renounce within the border."));
            }
            if (ps.isButtered) {
                GameState.getPlayerState(player).isButtered = false;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Renounced butter."));
                // Call function to remove butter hat
                
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Renounce nothing"));
            }
            
        }
        return true;
    }
    
}
