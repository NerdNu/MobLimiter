package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

    private Map<String, Integer> limits;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        limits = loadConfig();
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(getDescription().getName() + " " + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info(getDescription().getName() + " " + getDescription().getVersion() + " disabled.");
    }
    
    public Map<String, Integer> loadConfig() {
//       All Mobs we care about
//       Blaze, CaveSpider, Creeper, Enderman, Giant, PigZombie, Silverfish, Skeleton, Spider, Zombie
//       Chicken, Cow, MushroomCow, Ocelot, Pig, Sheep, Wolf

         Map<String, Integer> limconf = new HashMap<String, Integer>();
//       Animals
         limconf.put("Chicken", getConfig().getInt("limit.Chicken", 10));
         limconf.put("Cow", getConfig().getInt("limit.Cow", 10));
         limconf.put("MushroomCow", getConfig().getInt("limit.MushroomCow", 10));
         limconf.put("Ocelot", getConfig().getInt("limit.Ocelot", 10));
         limconf.put("Pig", getConfig().getInt("limit.Pig", 10));
         limconf.put("Sheep", getConfig().getInt("limit.Sheep", 10));
         limconf.put("Wolf", getConfig().getInt("limit.Wolf", 10));
//       Monsters
         limconf.put("Blaze", getConfig().getInt("limit.Blaze", 10));
         limconf.put("CaveSpider", getConfig().getInt("limit.CaveSpider", 10));
         limconf.put("Creeper", getConfig().getInt("limit.Creeper", 10));
         limconf.put("Enderman", getConfig().getInt("limit.Enderman", 10));
         limconf.put("Giant", getConfig().getInt("limit.Giant", 10));
         limconf.put("PigZombie", getConfig().getInt("limit.PigZombie", 10));
         limconf.put("Silverfish", getConfig().getInt("limit.Silverfish", 10));
         limconf.put("Skeleton", getConfig().getInt("limit.Skeleton", 10));
         limconf.put("Spider", getConfig().getInt("limit.Spider", 10));
         limconf.put("Zombie", getConfig().getInt("limit.Zombie", 10));
         return limconf;
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent e) {
        Map<String, Integer> count = new HashMap<String, Integer>();
        for( Entity entity : e.getChunk().getEntities()) {
            if((entity instanceof Animals) || (entity instanceof Monster)) {
                if(count.get(entity.getType().name()) == null) {
                    count.put(entity.getType().name(), 0);
                }
                int mbcount = count.get(entity.getType().name());
                count.put(entity.getType().name(), ++mbcount);
                if(count.get(entity.getType().name()) != null) {
                    if(mbcount > count.get(entity.getType().name())) {
                        entity.remove();
                    }
                }
            }
        }
    }
}
