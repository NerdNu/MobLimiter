package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.EntityHelper;
import nu.nerd.moblimiter.MobLimiter;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Cull applicable mobs on chunk unload.
 * This is based on behavior from the original MobLimiter 1.x.
 */
public class EntityUnloadLimiter implements Listener {


    private MobLimiter plugin;

    private Chunk chunk;


    public EntityUnloadLimiter() {
        plugin = MobLimiter.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntitiesUnloaded(EntitiesUnloadEvent event) {
        removeMobs(event.getEntities());
    }


    /**
     * Remove excess mobs in a chunk, if chunk unload culling is enabled for the mob type.
     * This is the default behavior from MobLimiter 1.x.
     * @param entities The list of entities being unloaded
     */
    private void removeMobs(List<Entity> entities) {
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (Entity entity : entities) {

            // Constrain entities to be removed to limitable mobs, excluding villagers
            if (entity.isDead() || !EntityHelper.isLimitableMob(entity) || entity instanceof Villager) continue;

            // Exempt special mobs
            if (EntityHelper.isSpecialMob((LivingEntity) entity)) {
                if (plugin.getConfiguration().debug()) {
                    plugin.getLogger().info("Special mob exempted from removal: " + EntityHelper.getMobDescription(entity));
                }
                continue;
            }

            ConfiguredMob limits = plugin.getConfiguration().getLimits(entity);
            String key = limits.getKey();
            int cap = limits.getCull();

            Integer oldCount = count.get(key);
            int mobCount = (oldCount == null) ? 1 : oldCount + 1;
            count.put(key, mobCount);

            if (cap > -1 && mobCount > cap) {
                entity.remove();
                if (plugin.getConfiguration().debug()) {
                    plugin.getLogger().info("Chunk unload removed: " + EntityHelper.getMobDescription(entity));
                }
            }

        }
    }


    /**
     * Run removeMobs() on all loaded chunks
     */
    public void removeAllMobs() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                removeMobs(Arrays.asList(chunk.getEntities()));
            }
        }
    }


}
