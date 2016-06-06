package nu.nerd.moblimiter;

import org.bukkit.plugin.java.JavaPlugin;


public class MobLimiter extends JavaPlugin {


    public static MobLimiter instance;
    private Configuration configuration;


    public void onEnable() {
        MobLimiter.instance = this;
        configuration = new Configuration();
    }


    public Configuration getConfiguration() {
        return configuration;
    }


}
