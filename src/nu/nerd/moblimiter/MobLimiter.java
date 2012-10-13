package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

    public Map<String, Integer> limits = new HashMap<String, Integer>();

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        for (Map.Entry<String, Object> entry : this.getConfig().getValues(false).entrySet()) {
            if(entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof Integer) {
                limits.put(entry.getKey(), (Integer)entry.getValue());
            }
        }
        this.saveConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (Chunk c : getServer().getWorlds().get(0).getLoadedChunks()) {
            removeMobs(c);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(final ChunkUnloadEvent e) {
        removeMobs(e.getChunk());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawnEvent(final CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Animals) {
            if (e.getSpawnReason() == SpawnReason.BREEDING) {
                ((Animals) e.getEntity()).setBreed(true);
                for (Entity en : e.getEntity().getNearbyEntities(4, 4, 4)) {
                    if (en instanceof Animals) {
                        ((Animals) en).setBreed(true);
                    }
                }
            }
        }
    }

    public void removeMobs(Chunk c) {
        net.minecraft.server.Chunk chunk = ((CraftChunk) c).getHandle();
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < chunk.entitySlices[i].size(); j++) {
                Object obj = chunk.entitySlices[i].get(j);
                Entity entity = null;
                if (obj instanceof net.minecraft.server.Entity) {
                    entity = ((net.minecraft.server.Entity) obj).getBukkitEntity();
                } else {
                    entity = (Entity) obj;
                }
                if (entity != null) {
                    if ((entity instanceof Animals) || (entity instanceof Monster)) {
                        String mobname = entity.getType().name().toLowerCase();
                        if (entity instanceof Sheep) {
                            mobname += ((Sheep) entity).getColor().name().toLowerCase();
                        }
                        if (count.get(mobname) == null) {
                            count.put(mobname, 0);
                        }
                        int mbcount = count.get(mobname);
                        count.put(mobname, ++mbcount);
                        if (limits.get(mobname) != null) {
                            if (mbcount > limits.get(mobname)) {
                                ((LivingEntity) entity).remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
