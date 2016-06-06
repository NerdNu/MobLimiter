package nu.nerd.moblimiter;

import nu.nerd.moblimiter.configuration.Configuration;
import nu.nerd.moblimiter.limiters.AgeLimiter;
import nu.nerd.moblimiter.limiters.ChunkUnloadLimiter;
import nu.nerd.moblimiter.limiters.SpawnLimiter;
import org.bukkit.plugin.java.JavaPlugin;


public class MobLimiter extends JavaPlugin {


    public static MobLimiter instance;
    private Configuration configuration;


    public void onEnable() {
        MobLimiter.instance = this;
        configuration = new Configuration();
        new AgeLimiter();
        new SpawnLimiter();
        new ChunkUnloadLimiter();
    }


    public Configuration getConfiguration() {
        return configuration;
    }


}
