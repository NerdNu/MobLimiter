package nu.nerd.moblimiter.configuration;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import javax.naming.ConfigurationException;


/**
 * Represents configured limits for a specific mob type
 */
public class ConfiguredMob {


    private EntityType type;
    private int age;
    private int max;
    private int chunkMax;
    private int cull;


    /**
     * Construct a ConfiguredMob from the YAML configuration. Unspecified values will fall back to the values
     * defined in the default block.
     * @param mob The configuration section
     * @param defaults The default values to fall back to
     * @throws ConfigurationException
     */
    public ConfiguredMob(ConfigurationSection mob, ConfiguredDefaults defaults) throws ConfigurationException{
        try {
            type = EntityType.valueOf(mob.getName().toUpperCase());
            age = mob.getInt("age", defaults.getAge());
            max = mob.getInt("max", defaults.getMax());
            chunkMax = mob.getInt("chunk_max", defaults.getChunkMax());
            cull = mob.getInt("cull", defaults.getCull());
        } catch (Exception ex) {
            throw new ConfigurationException("Invalid configuration for mob type: " + mob.getName());
        }
    }


    /**
     * Create a dummy ConfiguredMob with default values, for when one isn't actually configured
     * @param entityType The entity type
     * @param defaults The default values
     */
    public ConfiguredMob(EntityType entityType, ConfiguredDefaults defaults) {
        type = entityType;
        age = defaults.getAge();
        max = defaults.getMax();
        chunkMax = defaults.getChunkMax();
        cull = defaults.getCull();
    }



    /**
     * The Bukkit EntityType these limits apply to
     */
    public EntityType getType() {
        return type;
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
