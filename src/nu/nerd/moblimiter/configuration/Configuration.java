package nu.nerd.moblimiter.configuration;


import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import javax.naming.ConfigurationException;
import java.util.HashMap;

public class Configuration {


    private MobLimiter plugin;
    private int radius;
    private boolean debug;
    private int breedingTicks;
    private int growthTicks;
    private ConfiguredDefaults defaults;
    private HashMap<EntityType, ConfiguredMob> limits;


    public Configuration() {
        plugin = MobLimiter.instance;
        plugin.saveDefaultConfig();
        load();
    }


    /**
     * Reload the configuration from disk
     */
    public void load() {

        plugin.reloadConfig();
        this.radius = plugin.getConfig().getInt("radius", 3);
        this.debug = plugin.getConfig().getBoolean("debug", false);
        this.breedingTicks = plugin.getConfig().getInt("breeding_ticks", 300);
        this.growthTicks = plugin.getConfig().getInt("growth_ticks", 300);
        this.defaults = new ConfiguredDefaults(plugin.getConfig());

        this.limits = new HashMap<EntityType, ConfiguredMob>();
        ConfigurationSection mobLimits = plugin.getConfig().getConfigurationSection("limits");
        if (mobLimits != null) {
            for (String key : mobLimits.getKeys(false)) {
                try {
                    ConfigurationSection l = mobLimits.getConfigurationSection(String.format("limits.%s", key));
                    ConfiguredMob mob = new ConfiguredMob(l, defaults);
                    limits.put(mob.getType(), mob);
                } catch (ConfigurationException ex) {
                    plugin.getLogger().warning(ex.getMessage());
                }
            }
        }

    }


    /**
     * Get the "view distance," in chunks, to check for mobs when a new mob spawns.
     */
    public int getRadius() {
        return radius;
    }


    /**
     * Is debug mode on?
     */
    public boolean debug() {
        return debug;
    }


    /**
     * Ticks until a farm animal is ready to breed again.
     */
    public int getBreedingTicks() {
        return breedingTicks;
    }


    /**
     * Ticks until a farm animal grows up.
     */
    public int getGrowthTicks() {
        return growthTicks;
    }


    /**
     * Get the global default limits
     */
    public ConfiguredDefaults getDefaults() {
        return defaults;
    }


    /**
     * Get the limits for a specific mob type, gracefully falling back to values from the "default" block
     */
    public ConfiguredMob getLimits(EntityType type) {
        if (limits.containsKey(type)) {
            return limits.get(type);
        } else {
            return new ConfiguredMob(type, defaults);
        }
    }


}
