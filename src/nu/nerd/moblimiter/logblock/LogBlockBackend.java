package nu.nerd.moblimiter.logblock;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;


/**
 * Handle communication with the LogBlock API.
 * This will be used by LogBlockWrapper to safely avoid creating a hard dependency.
 */
public class LogBlockBackend {


    private MobLimiter plugin;
    private Consumer lbConsumer = null;


    public LogBlockBackend() {
        plugin = MobLimiter.instance;
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("LogBlock");
        if (plugin != null) lbConsumer = ((LogBlock) plugin).getConsumer();
    }


    /**
     * Log an entity removal to LogBlock with the reason specified
     * @param entity The entity being removed
     * @param reason The "name" LogBlock will store. e.g. MobLimiterCull
     */
    public void logEntityRemoval(Entity entity, String reason) {
        if (lbConsumer != null) {
            Actor killer = new Actor(reason);
            Actor victim = Actor.actorFromEntity(entity);
            lbConsumer.queueKill(entity.getLocation(), killer, victim, 0);
        }
    }


}
