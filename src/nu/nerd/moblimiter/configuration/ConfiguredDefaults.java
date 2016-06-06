package nu.nerd.moblimiter.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import javax.naming.ConfigurationException;


/**
 * Represents global default limits that apply to any mob type, unless a ConfiguredMob overrides.
 */
public class ConfiguredDefaults {


    private int age;
    private int max;
    private int chunkMax;
    private int cull;


    public ConfiguredDefaults(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("defaults");
        if (section != null) {
            age = section.getInt("age", -1);
            max = section.getInt("max", -1);
            chunkMax = section.getInt("chunk_max", -1);
            cull = section.getInt("cull", -1);
        } else {
            age = -1;
            max = -1;
            chunkMax = -1;
            cull = -1;
        }
    }


    /**
     * The maximum age, in ticks, that this mob should live for before being removed
     */
    public int getAge() {
        return age;
    }


    /**
     * The maximum number of this mob type that should be allowed to exist within a
     * view distance centered on a spawning mob.
     */
    public int getMax() {
        return max;
    }


    /**
     * The maximum number of this mob type that should be allowed to exist in a single chunk.
     */
    public int getChunkMax() {
        return chunkMax;
    }


    /**
     * The number of mobs that should be left after a chunk unload cull
     */
    public int getCull() {
        return cull;
    }


}
