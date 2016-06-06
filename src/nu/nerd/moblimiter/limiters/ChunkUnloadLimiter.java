package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.event.Listener;


/**
 * Cull applicable mobs on chunk unload.
 * This is based on behavior from the original MobLimiter 1.x.
 */
public class ChunkUnloadLimiter implements Listener {


    private MobLimiter plugin;


    public ChunkUnloadLimiter() {
        plugin = MobLimiter.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


}
