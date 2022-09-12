package slard.craft;

import org.bukkit.plugin.java.JavaPlugin;

public class BordSmpPlugin extends JavaPlugin {
    public static boolean DEBUG;

    @Override
    public void onEnable() {
        if (System.getenv("SLARD_DEBUG") == null) {
            DEBUG = false;
        }
        DEBUG = Boolean.parseBoolean(System.getenv("SLARD_DEBUG"));
        getServer().getPluginManager().registerEvents(new GameStateListener(this), this);
        this.getCommand("inborder").setExecutor(new InBorderCommand(this));
    }

    // Fired when plugin is disabled
    @Override
    public void onDisable() {
    }

}