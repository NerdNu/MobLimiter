package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Sheep;
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
//       WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK
        
        
        Map<String, Integer> limconf = new HashMap<String, Integer>();
//       Animals
        limconf.put("chicken", getConfig().getInt("limit.chicken", 4));
        limconf.put("cow", getConfig().getInt("limit.cow", 4));
        limconf.put("mushroom_cow", getConfig().getInt("limit.mushroomcow", 4));
        limconf.put("ocelot", getConfig().getInt("limit.ocelot", 4));
        limconf.put("pig", getConfig().getInt("limit.pig", 4));
        limconf.put("wolf", getConfig().getInt("limit.wolf", 4));
//       Sheeeeeeepppp
        limconf.put("sheepwhite", getConfig().getInt("limit.sheep.white", 4));
        limconf.put("sheeporange", getConfig().getInt("limit.sheep.orange", 2));
        limconf.put("sheepmagenta", getConfig().getInt("limit.sheep.magenta", 2));
        limconf.put("sheeplight_blue", getConfig().getInt("limit.sheep.light_blue", 2));
        limconf.put("sheepyellow", getConfig().getInt("limit.sheep.yellow", 2));
        limconf.put("sheeplime", getConfig().getInt("limit.sheep.lime", 2));
        limconf.put("sheeppink", getConfig().getInt("limit.sheep.pink", 2));
        limconf.put("sheepgray", getConfig().getInt("limit.sheep.gray", 2));
        limconf.put("sheepsilver", getConfig().getInt("limit.sheep.silver", 2));
        limconf.put("sheepcyan", getConfig().getInt("limit.sheep.cyan", 2));
        limconf.put("sheeppurple", getConfig().getInt("limit.sheep.purple", 2));
        limconf.put("sheepblue", getConfig().getInt("limit.sheep.blue", 2));
        limconf.put("sheepbrown", getConfig().getInt("limit.sheep.brown", 2));
        limconf.put("sheepgreen", getConfig().getInt("limit.sheep.green", 2));
        limconf.put("sheepred", getConfig().getInt("limit.sheep.red", 2));
        limconf.put("sheepblack", getConfig().getInt("limit.sheep.black", 2));
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
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                synchronized(e.getWorld()) {
                    Entity[] entlist = e.getChunk().getEntities();
                    Map<String, Integer> count = new HashMap<String, Integer>();
                    for (Entity entity : entlist) {
                        if ((entity instanceof Animals) || (entity instanceof Monster)) {
                            String mobname = entity.getType().name().toLowerCase();
                            if (entity instanceof Sheep) {
                                mobname += ((Sheep)entity).getColor().name().toLowerCase();
                            }
                            if (count.get(mobname) == null) {
                                count.put(mobname, 0);
                            }
                            int mbcount = count.get(mobname);
                            count.put(mobname, ++mbcount);
                            if (limits.get(mobname) != null) {
                                if (mbcount > limits.get(mobname)) {
                                    entity.remove();
                                    System.out.println("Removed entity " + mobname);
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
