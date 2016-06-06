package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * Sweep loaded chunks every 30 seconds and remove applicable mobs over their type's age limit
 */
public class AgeLimiter extends BukkitRunnable {


    private MobLimiter plugin;


    public AgeLimiter() {
        plugin = MobLimiter.instance;
        this.runTaskTimer(plugin, 600L, 600L); //sweep every 30 seconds
    }


    public void run() {
        //run age limit sweeps on loaded chunks
    }


}
