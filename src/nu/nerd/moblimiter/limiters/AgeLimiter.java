package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.MobLimiter;
import nu.nerd.moblimiter.EntityHelper;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
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
        if (plugin.getConfiguration().debug()) {
            plugin.getLogger().info(String.format("Age limit sweep removed %d entities", removed));
        }
    }


    /**
     * Sweep the chunk and remove mobs that are past their shelf life
     * @param chunk the chunk to check
     */
    private void sweepChunk(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {

            // Only Animals and Monsters are eligible for removal
            if (entity.isDead() || !(entity instanceof Animals || entity instanceof Monster)) continue;

            // Exempt special mobs
            if (EntityHelper.isSpecialMob((LivingEntity) entity)) {
                if (plugin.getConfiguration().debug()) {
                    plugin.getLogger().info("Special mob exempted from removal: " + EntityHelper.getMobDescription(entity));
                }
                continue;
            }

            // Leave two of any farm animals
            if (EntityHelper.isBreedingPair(entity)) continue;

            // Remove mobs
            ConfiguredMob limits = plugin.getConfiguration().getLimits(entity);
            if (!entity.isDead() && entity.getTicksLived() > limits.getAge() && limits.getAge() > -1) {
                ((LivingEntity) entity).damage(1000); // Kill the entity and drop its items
                removed++;
                if (plugin.getConfiguration().debug()) {
                    plugin.getLogger().info("Removed mob (age limit): " + EntityHelper.getMobDescription(entity));
                }
            }

        }
    }


}
