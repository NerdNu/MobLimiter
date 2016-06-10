package nu.nerd.moblimiter.logblock;

import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;


/**
 * Wraps LogBlock API calls so it can be a soft dependency
 */
public class LogBlockWrapper {


    private MobLimiter plugin;
    private LogBlockBackend logBlock = null;


    public LogBlockWrapper() {
        plugin = MobLimiter.instance;
        if (plugin.getConfiguration().logBlockEnabled() && logBlockPresent()) {
            logBlock = new LogBlockBackend();
        }
    }


    /**
     * Log an entity removal to LogBlock with the reason specified
     * @param entity The entity being removed
     */
    public void logEntityRemoval(Entity entity) {
        if (logBlock != null) {
            logBlock.logEntityRemoval(entity, "MobLimiter");
        }
    }


    private boolean logBlockPresent() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("LogBlock");
        return (plugin != null);
    }


}
