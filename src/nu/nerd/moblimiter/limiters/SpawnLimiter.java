package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.EntityHelper;
import nu.nerd.moblimiter.MobLimiter;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.ArrayList;
import java.util.List;


/**
 * Limit newly spawning mobs if there are too many
 */
public class SpawnLimiter implements Listener {


    private MobLimiter plugin;
    private List<SpawnReason> reasons;


    public SpawnLimiter() {
        plugin = MobLimiter.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reasons = new ArrayList<SpawnReason>();
        reasons.add(SpawnReason.BREEDING);
        reasons.add(SpawnReason.DEFAULT);
        reasons.add(SpawnReason.NATURAL);
        reasons.add(SpawnReason.SPAWNER);
    }


    /**
     * Handle mob limting on creature spawn
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawnEvent(final CreatureSpawnEvent event) {

        SpawnReason reason = event.getSpawnReason();
        EntityType type = event.getEntity().getType();
        ConfiguredMob limits = plugin.getConfiguration().getLimits(type);

        if (!reasons.contains(reason)) return;

        // Cancel spawn of mobs over the radius limit
        if (countEntitiesInSpawnRadius(event.getEntity()) >= limits.getMax() && limits.getMax() > -1) {
            log(event.getEntity(), reason, "radius", limits.getMax());
            event.getEntity().remove();
        }

        // Cancel spawn of mobs over the chunk limit
        if (countEntitiesInChunk(event.getEntity()) >= limits.getChunkMax() && limits.getChunkMax() > -1) {
            log(event.getEntity(), reason, "chunk", limits.getChunkMax());
            event.getEntity().remove();
        }

    }


    /**
     * Count entities of the same type within a "view distance" in chunks from the original entity
     * @param entity the entity to check
     * @return number of matching entities
     */
    private int countEntitiesInSpawnRadius(Entity entity) {
        int count = 0;
        int radius = plugin.getConfiguration().getRadius();
        World world = entity.getWorld();
        Chunk start = entity.getLocation().getChunk();
        for (int x = start.getX() - radius; x <= start.getX() + radius; x++) {
            for (int z = start.getZ() - radius; z <= start.getZ() + radius; z++) {
                Chunk c = world.getChunkAt(x, z);
                for (Entity e : c.getEntities()) {
                    if (e.getType() == entity.getType()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }


    /**
     * Count entities of the same type in an individual chunk
     * @param entity The entity to check
     * @return number of matching entities
     */
    private int countEntitiesInChunk(Entity entity) {
        int count = 0;
        for (Entity e : entity.getLocation().getChunk().getEntities()) {
            if (e.getType() == entity.getType()) {
                count++;
            }
        }
        return count;
    }


    /**
     * Log entity removal for diagnostic purposes if debug mode is on
     */
    private void log(Entity entity, SpawnReason reason, String capType, int cap) {
        if (!plugin.getConfiguration().debug()) return;
        String mob = EntityHelper.getMobDescription(entity);
        String details = String.format("[reason: %s, cap type: %s, cap: %d]", reason.toString(), capType, cap);
        String msg = String.format("Cancelled spawn of %s %s", mob, details);
        plugin.getLogger().info(msg);
    }


}
