package nu.nerd.moblimiter.configuration;


import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configuration {


    private MobLimiter plugin;
    private int radius;
    private boolean debug;
    private int breedingTicks;
    private int growthTicks;
    private boolean logBlock;
    private boolean relativeAge;
    private ConfiguredDefaults defaults;
    private HashMap<String, ConfiguredMob> limits;
    private List<Material> spawnEggs = new ArrayList<>();


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
        this.logBlock = plugin.getConfig().getBoolean("logblock", false);
        this.relativeAge = plugin.getConfig().getBoolean("relative_age", false);

        spawnEggs = new ArrayList<>();

        this.limits = new HashMap<String, ConfiguredMob>();
        ConfigurationSection mobLimits = plugin.getConfig().getConfigurationSection("limits");
        if (mobLimits != null) {
            for (String key : mobLimits.getKeys(false)) {
                try {
                    ConfigurationSection l = mobLimits.getConfigurationSection(key);
                    ConfiguredMob mob;
                    if (l != null) {
                        mob = new ConfiguredMob(l, defaults);
                    } else {
                        mob = new ConfiguredMob(key, defaults); //use default values for YAML "key: []" blocks
                    }
                    limits.put(key.toUpperCase(), mob);
                } catch (ConfigurationException ex) {
                    plugin.getLogger().warning(ex.getMessage());
                }
            }
        }

        for(String egg : plugin.getConfig().getStringList("spawn_eggs")) {
            spawnEggs.add(Material.getMaterial(egg));
        }
        System.out.println(spawnEggs);

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
     * Whether support for LogBlock entity removal logging is enabled
     * @return true if enabled
     */
    public boolean logBlockEnabled() {
        return logBlock;
    }


    /**
     * Whether mobs' age should be relative to the last time they target a player
     * @return true if enabled
     */
    public boolean relativeAgeEnabled() {
        return relativeAge;
    }


    /**
     * Get the global default limits
     */
    public ConfiguredDefaults getDefaults() {
        return defaults;
    }

    /**
     * Gets a list of spawn eggs as defined in the config
     * @return A list of spawn eggs as Strings
     */
    public List<Material> getSpawnEggs(){return spawnEggs;}


    /**
     * Get the limits for a specific mob type, gracefully falling back to values from the "default" block
     */
    public ConfiguredMob getLimits(Entity entity) {
        String key = entity.getType().toString();
        if (entity instanceof Sheep) {
            key = key + "_" + ((Sheep) entity).getColor().name().toUpperCase();
        }
        if (limits.containsKey(key)) {
            return limits.get(key);
        } else {
            return new ConfiguredMob(key, defaults);
        }
    }


    public HashMap<String, ConfiguredMob> getAllLimits() {
        return limits;
    }


}
