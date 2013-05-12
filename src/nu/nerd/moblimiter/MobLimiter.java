package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_5_R3.CraftChunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

    public Map<String, Integer> limits = new HashMap<String, Integer>();
    public int ageCapBaby = -1;
    public int ageCapBreed = -1;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        for (Map.Entry<String, Object> entry : this.getConfig().getValues(false).entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof Integer) {
                if (entry.getKey().equals("agecapbaby")) {
                    ageCapBaby = (Integer) entry.getValue();
                } else if (entry.getKey().equals("agecapbreed")) {
                    ageCapBreed = (Integer) entry.getValue();
                } else {
                    limits.put(entry.getKey(), (Integer) entry.getValue());
                }
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
        if ((e.getSpawnReason() == SpawnReason.BREEDING || e.getSpawnReason() == SpawnReason.EGG) && isFarmAnimal(e.getEntity())) {
            applyAgeCap((Animals) e.getEntity());
            for (Entity en : e.getEntity().getNearbyEntities(4, 4, 4)) {
                if (isFarmAnimal(en)) {
                    applyAgeCap((Animals) en);
                }
            }
        }
    }

    public boolean isFarmAnimal(Entity en) {
        return (en instanceof Animals) && !(en instanceof Tameable);
    }

    public void applyAgeCap(Animals en) {
        if (ageCapBaby >= 0 && !en.isAdult()) {
            en.setAge(Math.max(en.getAge(), -ageCapBaby));
        } else if (ageCapBreed >= 0 && en.isAdult()) {
            en.setAge(Math.min(en.getAge(), ageCapBreed));
        }
    }

    public void removeMobs(Chunk c) {
        net.minecraft.server.v1_5_R3.Chunk chunk = ((CraftChunk) c).getHandle();
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < chunk.entitySlices[i].size(); j++) {
                Object obj = chunk.entitySlices[i].get(j);
                Entity entity = null;
                if (obj instanceof net.minecraft.server.v1_5_R3.Entity) {
                    entity = ((net.minecraft.server.v1_5_R3.Entity) obj).getBukkitEntity();
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
