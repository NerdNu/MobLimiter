package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.MobLimiter;
import nu.nerd.moblimiter.EntityHelper;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Sweep loaded chunks every 30 seconds and remove applicable mobs over their type's age limit
 */
public class AgeLimiter extends BukkitRunnable implements Listener {


    private MobLimiter plugin;
    private int removed;
    private Map<UUID, Integer> lastTargeted;


    public AgeLimiter() {
        plugin = MobLimiter.instance;
        lastTargeted = new HashMap<UUID, Integer>();
        this.runTaskTimer(plugin, 600L, 600L); //sweep every 30 seconds
    }


    /**
     * Call the sweep on every loaded chunk in every world
     */
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

            // Constrain entities to be removed to limitable mobs, excluding villagers
            if (entity.isDead() || !EntityHelper.isLimitableMob(entity) || entity instanceof Villager) continue;

            // Exempt special mobs
            if (EntityHelper.isSpecialMob((LivingEntity) entity)) {
                if (plugin.getConfiguration().debug()) {
                    plugin.getLogger().info("Special mob exempted from removal: " + EntityHelper.getMobDescription(entity));
                }
                continue;
            }

            // Leave two of any farm animals
            if (EntityHelper.isBreedingPair(entity)) continue;

            // If relative ages are on, and the mob is targeting a player, don't remove it
            if (isTargetingPlayer(entity)) continue;

            // Remove mobs
            ConfiguredMob limits = plugin.getConfiguration().getLimits(entity);
            if (!entity.isDead() && adjustedAge(entity) > limits.getAge() && limits.getAge() > -1) {
                ((LivingEntity) entity).damage(1000); // Kill the entity and drop its items
                removed++;
                lastTargeted.remove(entity.getUniqueId());
                plugin.getLogBlock().logEntityRemoval(entity, 347);
                if (plugin.getConfiguration().debug()) {
                    plugin.getLogger().info("Removed mob (age limit): " + EntityHelper.getMobDescription(entity));
                }
            }

        }
    }


    /**
     * Don't let the sweep kill entities currently targeting a player.
     * Also, keep resetting the relative age, since EntityTargetEvent
     * only fires when the target changes.
     * @param entity the entity to check
     * @return true if the Entity is a Creature type and is targeting a player.
     */
    private boolean isTargetingPlayer(Entity entity) {
        if (!plugin.getConfiguration().relativeAgeEnabled()) return false;
        if (entity instanceof Creature) {
            Creature creature = (Creature) entity;
            if (creature.getTarget() != null && creature.getTarget().getType().equals(EntityType.PLAYER)) {
                lastTargeted.put(entity.getUniqueId(), entity.getTicksLived());
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the adjusted age of a mob.
     * If relative age is on, and the mob recently tageted a player, the delta ticks will be returned.
     * Otherwise, the regular getTicksLived() will be used.
     * @param entity the entity to check
     * @return the age in ticks
     */
    public int adjustedAge(Entity entity) {
        if (lastTargeted.containsKey(entity.getUniqueId())) {
            return entity.getTicksLived() - lastTargeted.get(entity.getUniqueId());
        } else {
            return entity.getTicksLived();
        }
    }


}
