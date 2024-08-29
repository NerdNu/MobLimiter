package nu.nerd.moblimiter;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import nu.nerd.moblimiter.configuration.Configuration;
import nu.nerd.moblimiter.limiters.AgeLimiter;
import nu.nerd.moblimiter.limiters.EntityUnloadLimiter;
import nu.nerd.moblimiter.limiters.SpawnLimiter;
import nu.nerd.moblimiter.listeners.BreedingListener;
import nu.nerd.moblimiter.logblock.LogBlockWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


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
        new BreedingListener();
        new CommandHandler();
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
