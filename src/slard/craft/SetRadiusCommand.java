package slard.craft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetRadiusCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("/setradius needs exactly 1 arg of radius.");
            return false;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.isOp()) {
                player.sendMessage("Must be op to use that command.");
                return true;
            }
        }
        int newRadius;
        try {
            newRadius = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(args[0] + " is not parsable as integer.");
            return false;
        }
        GameStateListener.BORDER_RADIUS = newRadius;
        sender.sendMessage("Radius set to: " + args[0]);
        return true;
    }

}
