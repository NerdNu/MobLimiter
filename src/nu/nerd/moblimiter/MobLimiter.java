package nu.nerd.moblimiter;

import nu.nerd.moblimiter.configuration.Configuration;
import nu.nerd.moblimiter.limiters.AgeLimiter;
import nu.nerd.moblimiter.limiters.EntityUnloadLimiter;
import nu.nerd.moblimiter.limiters.SpawnLimiter;
import nu.nerd.moblimiter.listeners.ClickEvents;
import nu.nerd.moblimiter.listeners.EntityEvents;
import nu.nerd.moblimiter.logblock.LogBlockWrapper;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;



public class MobLimiter extends JavaPlugin {


    public static MobLimiter instance;
    private Configuration configuration;
    private EntityUnloadLimiter chunkUnloadLimiter;
    private AgeLimiter ageLimiter;
    private LogBlockWrapper logBlock;


    public void onEnable() {
        MobLimiter.instance = this;
        configuration = new Configuration();
        chunkUnloadLimiter = new EntityUnloadLimiter();
        ageLimiter = new AgeLimiter();
        logBlock = new LogBlockWrapper();
        new SpawnLimiter();
        new ClickEvents();
        new EntityEvents();
        new CommandHandler();
        System.out.println(configuration.getSpawnEggs());
    }


    public void onDisable() {
        chunkUnloadLimiter.removeAllMobs();
    }


    public Configuration getConfiguration() {
        return configuration;
    }


    public AgeLimiter getAgeLimiter() {
        return ageLimiter;
    }


    public LogBlockWrapper getLogBlock() {
        return logBlock;
    }

    public static String locationToString(Location location) {
        return String.format("X: %.2f Y: %.2f Z: %.2f World: %s",
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getWorld().getName());
    }


}
