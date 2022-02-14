package slard.craft.town;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import slard.craft.SlardcraftPlugin;

public class GameState {

    public static Map<UUID, PlayerState> players = new HashMap<>();

    public static void initializeGameState() {
        // TODO this should account for all online players too
        try {
            FileInputStream fileIn = new FileInputStream("slardgamestate.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            players = (Map<UUID, PlayerState>) in.readObject();
            in.close();
            fileIn.close();
            System.out.printf("Game state loaded from slardgamestate.ser\n");
            System.out.printf("Game state: " + toStringPlayerStates());
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Map<UUID, PlayerState> not found\n");
            c.printStackTrace();
            return;
        }
    }

    public static String toStringPlayerStates() {
        String n = "";
        for (Map.Entry<UUID, PlayerState> entry : players.entrySet())
            n += "UUID: " + entry.getKey().toString() + "isButtered: " + entry.getValue().isButtered + " inTown: " + entry.getValue().inTown + "\n";
        return n;
    }

    public static void saveGameState() {
        //serializable kind of sucks bc any change you make you now have to create a whole new ass file. should switch over to json instead
        try {
            FileOutputStream fileOut = new FileOutputStream("slardgamestate.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(players);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in slardgamestate.ser\n");
        } 
        catch (IOException  i) {
            i.printStackTrace();
            System.out.printf("Failed to save game state.");
            if (SlardcraftPlugin.DEBUG) Bukkit.broadcastMessage("Failed to save game state.\n");
        }
    }

    @Nullable
    public static PlayerState getPlayerState(UUID uuid) {
        return players.get(uuid);
    }

    public static PlayerState putPlayerState(UUID uuid, boolean isButtered, boolean inTown) {
        PlayerState ps = new PlayerState(isButtered, inTown);
        players.put(uuid, ps);
        return ps;
    }

    public static PlayerState getPlayerState(Player player) {
      UUID uuid = player.getUniqueId();
      return players.get(uuid);
    }


}

