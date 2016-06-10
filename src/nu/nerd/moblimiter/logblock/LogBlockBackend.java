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
     * @param name The "name" LogBlock will store. e.g. MobLimiterCull
     * @param itemId The item to ascribe the kill to. Zero for fist.
     */
    public void logEntityRemoval(Entity entity, String name, int itemId) {
        if (lbConsumer != null) {
            Actor killer = new Actor(name);
            Actor victim = Actor.actorFromEntity(entity);
            lbConsumer.queueKill(entity.getLocation(), killer, victim, itemId);
        }
    }


}
