package nu.nerd.moblimiter;

import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MobLimiter extends JavaPlugin implements Listener {

    private static int limit;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("MobLimiter.Max.LivingEntity", 100);
        limit = this.getConfig().getInt("MobLimiter.Max.LivingEntity");
        saveConfig();
        getLogger().info("MobLimiter max set to: " + limit);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent e) {
        // Enforce population cap on chunk load
        int Mobs = 0;
        net.minecraft.server.Chunk chunk = ((CraftChunk) e.getChunk()).getHandle();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < chunk.entitySlices[i].size(); j++) {
                final Entity entity = ((net.minecraft.server.Entity) chunk.entitySlices[i].get(j)).getBukkitEntity();
                // will get all passive mobs
                if (isLimitedEntity(entity) && !(entity instanceof Monster)) {
                    Mobs++;
                    if (Mobs > limit) {
                        ((LivingEntity) entity).remove();
                        //getLogger().info("Removed " + entity.getType());
                    }
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < chunk.entitySlices[i].size(); j++) {
                final Entity entity = ((net.minecraft.server.Entity) chunk.entitySlices[i].get(j)).getBukkitEntity();
                // run through hostile mobs after, the passive mobs should clear first, lets remove them before passive, keep the llama to a minimum.
                if (entity instanceof Monster) {
                    Mobs++;
                    if (Mobs > limit) {
                        ((LivingEntity) entity).remove();
                        //getLogger().info("Removed " + entity.getType());
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent e) {
        // Allow or deny creature spawn events based on the population of its chunk
        int count = 0;
        final Entity entity = e.getEntity();
        // Count limitable entities in chunk directly
        net.minecraft.server.Chunk chunk = ((CraftChunk) entity.getLocation().getChunk()).getHandle();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < chunk.entitySlices[i].size(); j++) {
                if (isLimitedEntity(chunk.entitySlices[i].get(j))) {
                    count++;
                }
            }
        }
        if (count > limit) {
            // Prevent spawning
            e.setCancelled(true);
            return;
        }
    }

    public boolean isLimitedEntity(final Object obj) {
        Entity entity = null;
        if (obj instanceof net.minecraft.server.Entity) {
            entity = ((net.minecraft.server.Entity) obj).getBukkitEntity();
        } else {
            entity = (Entity) obj;
        }
        if (entity != null && entity instanceof LivingEntity &&
            !(entity instanceof HumanEntity)) {
            return true;
        }
        return false;
    }

}
