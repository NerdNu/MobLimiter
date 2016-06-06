package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.MobLimiter;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * Sweep loaded chunks every 30 seconds and remove applicable mobs over their type's age limit
 */
public class AgeLimiter extends BukkitRunnable {


    private MobLimiter plugin;
    private int removed;


    public AgeLimiter() {
        plugin = MobLimiter.instance;
        this.runTaskTimer(plugin, 600L, 600L); //sweep every 30 seconds
    }


    public void run() {
        removed = 0;
        for (World world: plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                sweepChunk(chunk);
            }
        }
    }


    /**
     * Sweep the chunk and remove mobs that are past their shelf life
     * @param chunk the chunk to check
     */
    private void sweepChunk(Chunk chunk) {
        //todo: special mobs
        //todo: save breeding pairs of farm animals
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue; //skip non-living entities like item frames
            ConfiguredMob limits = plugin.getConfiguration().getLimits(entity.getType());
            if (entity.getTicksLived() > limits.getAge() && limits.getAge() > -1) {
                entity.remove();
                removed++;
            }
        }
    }


}
