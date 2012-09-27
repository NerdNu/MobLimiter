package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

    public Map<String, Integer> limits;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.limits = loadConfig();
        this.saveConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info(this.getDescription().getName() + " " + this.getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.getLogger().info(getDescription().getName() + " " + getDescription().getVersion() + " disabled.");
    }

    public Map<String, Integer> loadConfig() {
//       All Mobs we care about
//       Blaze, CaveSpider, Creeper, Enderman, Giant, PigZombie, Silverfish, Skeleton, Spider, Zombie
//       Chicken, Cow, MushroomCow, Ocelot, Pig, Sheep, Wolf

        Map<String, Integer> limconf = new HashMap<String, Integer>();
//       Animals
        limconf.put("chicken", getConfig().getInt("limit.chicken", 4));
        limconf.put("cow", getConfig().getInt("limit.cow", 4));
        limconf.put("mushroom_cow", getConfig().getInt("limit.mushroomcow", 4));
        limconf.put("ocelot", getConfig().getInt("limit.ocelot", 4));
        limconf.put("pig", getConfig().getInt("limit.pig", 4));
        limconf.put("sheep", getConfig().getInt("limit.sheep", 12));
        limconf.put("wolf", getConfig().getInt("limit.wolf", 4));
//       Monsters
        limconf.put("blaze", getConfig().getInt("limit.blaze", 4));
        limconf.put("cave_spider", getConfig().getInt("limit.cavespider", 4));
        limconf.put("creeper", getConfig().getInt("limit.creeper", 4));
        limconf.put("enderman", getConfig().getInt("limit.enderman", 4));
        limconf.put("giant", getConfig().getInt("limit.giant", 4));
        limconf.put("pig_zombie", getConfig().getInt("limit.pigzombie", 4));
        limconf.put("silverfish", getConfig().getInt("limit.silverfish", 4));
        limconf.put("skeleton", getConfig().getInt("limit.skeleton", 4));
        limconf.put("spider", getConfig().getInt("limit.spider", 4));
        limconf.put("zombie", getConfig().getInt("limit.zombie", 4));
        return limconf;
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent e) {
//        this.getLogger().info("Chunk unloading, removing mobs");
//        this.getLogger().info(this.limits.toString());
        e.setCancelled(true);
        this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
            private final Entity[] entlist = e.getChunk().getEntities();

            @Override
            public void run() {
                synchronized(entlist) {
                    Map<String, Integer> count = new HashMap<String, Integer>();
                    for (Entity entity : entlist) {
                        if ((entity instanceof Animals) || (entity instanceof Monster)) {
                            if (count.get(entity.getType().name()) == null) {
                                count.put(entity.getType().name(), 0);
                            }
                            int mbcount = count.get(entity.getType().name());
                            count.put(entity.getType().name(), ++mbcount);
                            if (limits.get(entity.getType().name().toLowerCase()) != null) {
                                if (mbcount > limits.get(entity.getType().name().toLowerCase())) {
                                    entity.remove();
                                    System.out.println("Removed entity " + entity.getType().name());
                                }
                            }
                        }
                    }
                    e.getChunk().unload(true, true); // (save, safe)
                }
            }
        });
    }
}
