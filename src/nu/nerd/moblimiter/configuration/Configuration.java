package nu.nerd.moblimiter.configuration;


import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;

import javax.naming.ConfigurationException;
import java.util.HashMap;

public class Configuration {


    private MobLimiter plugin;
    private int radius;
    private boolean debug;
    private int breedingTicks;
    private int growthTicks;
    private ConfiguredDefaults defaults;
    private HashMap<String, ConfiguredMob> limits;


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

        this.limits = new HashMap<String, ConfiguredMob>();
        ConfigurationSection mobLimits = plugin.getConfig().getConfigurationSection("limits");
        if (mobLimits != null) {
            for (String key : mobLimits.getKeys(false)) {
                try {
                    ConfigurationSection l = mobLimits.getConfigurationSection(String.format("limits.%s", key));
                    ConfiguredMob mob = new ConfiguredMob(l, defaults);
                    limits.put(key.toUpperCase(), mob);
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
    public ConfiguredMob getLimits(Entity entity) {
        String key = entity.getType().toString();
        if (entity instanceof Sheep && !((Sheep) entity).getColor().equals(DyeColor.WHITE)) {
            key = "DYED_SHEEP";
        }
        if (limits.containsKey(key)) {
            return limits.get(key);
        } else {
            return new ConfiguredMob(entity.getType(), defaults);
        }
    }


    public HashMap<String, ConfiguredMob> getAllLimits() {
        return limits;
    }


}
