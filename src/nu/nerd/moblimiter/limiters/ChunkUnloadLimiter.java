package nu.nerd.moblimiter.limiters;

import nu.nerd.moblimiter.EntityHelper;
import nu.nerd.moblimiter.MobLimiter;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.Map;


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


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        removeMobs(event.getChunk());
    }


    /**
     * Remove excess mobs in a chunk, if chunk unload culling is enabled for the mob type.
     * This is the default behavior from MobLimiter 1.x.
     * @param chunk The chunk to cull mobs in
     */
    private void removeMobs(Chunk chunk) {
        Map<String, Integer> count = new HashMap<String, Integer>();
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

            ConfiguredMob limits = plugin.getConfiguration().getLimits(entity);
            String key = entity.getType().toString();
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
                removeMobs(chunk);
            }
        }
    }


}
