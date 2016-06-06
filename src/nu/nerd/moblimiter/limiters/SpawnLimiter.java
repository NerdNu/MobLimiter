package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.event.Listener;


/**
 * Limit newly spawning mobs if there are too many
 */
public class SpawnLimiter implements Listener {


    private MobLimiter plugin;


    public SpawnLimiter() {
        plugin = MobLimiter.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


}
