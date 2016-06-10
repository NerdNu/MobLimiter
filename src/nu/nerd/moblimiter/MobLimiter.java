package nu.nerd.moblimiter;

import nu.nerd.moblimiter.configuration.Configuration;
import nu.nerd.moblimiter.limiters.AgeLimiter;
import nu.nerd.moblimiter.limiters.ChunkUnloadLimiter;
import nu.nerd.moblimiter.limiters.SpawnLimiter;
import nu.nerd.moblimiter.logblock.LogBlockWrapper;
import org.bukkit.plugin.java.JavaPlugin;


public class MobLimiter extends JavaPlugin {


    public static MobLimiter instance;
    private Configuration configuration;
    private ChunkUnloadLimiter chunkUnloadLimiter;
    private LogBlockWrapper logBlock;


    public void onEnable() {
        MobLimiter.instance = this;
        configuration = new Configuration();
        chunkUnloadLimiter = new ChunkUnloadLimiter();
        logBlock = new LogBlockWrapper();
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


    public LogBlockWrapper getLogBlock() {
        return logBlock;
    }


}
