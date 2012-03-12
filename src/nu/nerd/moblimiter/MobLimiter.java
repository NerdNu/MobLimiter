package nu.nerd.moblimiter;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin {

    @Override
    public void onEnable() {
    	this.getConfig().options().copyDefaults(true);
    	this.getConfig().addDefault("MobLimiter.Max.LivingEntity", 50);
        final int limit = this.getConfig().getInt("MobLimiter.Max.LivingEntity");
        saveConfig();
        getLogger().info("MobLimiter max set to: " + limit);
        // cleanup extra mobs once every 5 minutes
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            	getLogger().info("Running MobLimiter");
                for (World world : getServer().getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        int hostileMobs = 0;
                        for (Entity entity : chunk.getEntities()) {
                            // will get all mobs except players
                            if ((entity instanceof LivingEntity) && !(entity instanceof HumanEntity)) {
                                hostileMobs++;
                                if (hostileMobs > limit) {
                                	((LivingEntity) entity).remove();
                                }
                            }
                        }
                    }
                }
                getLogger().info("Done Running MobLimiter");
            }
        }, 1200, 6000);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
