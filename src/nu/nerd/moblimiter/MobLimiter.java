package nu.nerd.moblimiter;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin {

    @Override
    public void onEnable() {
        // cleanup extra mobs once every 5 minutes
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {
                for (World world : getServer().getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        int hostileMobs = 0;
                        for (Entity entity : chunk.getEntities()) {
                            // will get all hostile mobs except ghasts and slimes
                            if (entity instanceof Monster) {
                                hostileMobs++;
                                if (hostileMobs > 50) {
                                    ((CraftEntity) entity).getHandle().die();
                                }
                            }
                        }
                    }
                }
            }
        }, 1200, 6000);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
