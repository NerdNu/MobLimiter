package nu.nerd.moblimiter;

import nu.nerd.moblimiter.configuration.Configuration;
import nu.nerd.moblimiter.limiters.AgeLimiter;
import nu.nerd.moblimiter.limiters.ChunkUnloadLimiter;
import nu.nerd.moblimiter.limiters.SpawnLimiter;
import org.bukkit.plugin.java.JavaPlugin;


public class MobLimiter extends JavaPlugin {


    public static MobLimiter instance;
    private Configuration configuration;
    private ChunkUnloadLimiter chunkUnloadLimiter;


    public void onEnable() {
        MobLimiter.instance = this;
        configuration = new Configuration();
        chunkUnloadLimiter = new ChunkUnloadLimiter();
        new AgeLimiter();
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


}
