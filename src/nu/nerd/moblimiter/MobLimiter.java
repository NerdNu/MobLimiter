package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

    /**
     * Map from lower case EntityType.name() to max number of entities of that
     * type. Note that EntityType.name() is different from EntityType.getName().
     * The former is MUSHROOM_COW while the latter is MushroomCow.
     */
    protected Map<String, Integer> limits = new HashMap<String, Integer>();
    protected int ageCapBaby = -1;
    protected int ageCapBreed = -1;

    /**
     * If true, per-chunk mob caps are enforced for non-farm-animal mobs when
     * they spawn, in addition to when the chunk is unloaded.
     */
    protected boolean limitNaturalSpawn;

    /**
     * If true, the mob cap applies even when the mob was spawned from a
     * mob-spawner.
     */
    protected boolean limitSpawnerSpawn;

    /**
     * If true, cancelled spawns are shown in the server log.
     */
    protected boolean debugLimitSpawn;

    /**
     * If true, limits on numbers of each mob type are logged as the are parsed
     * from the configuration file.
     */
    protected boolean debugLimits;

    /**
     * If true, mobs removed during chunk unloading are logged.
     */
    protected boolean debugChunkUnload;

    /**
     * If true, do debug traces of the age cap code.
     */
    protected boolean debugAgeCap;

    /**
     * If true, do debug traces of preservation of special mobs on chunk unload.
     */
    protected boolean debugSpecial;

    /**
     * The set of Entity types whose caps are enforced when spawning.
     * 
     * Note that farm animals are explicitly excluded from that cap. Players can
     * breed them over the limit.
     */
    protected HashSet<EntityType> spawnLimitedEntityTypes = new HashSet<EntityType>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        limitNaturalSpawn = getConfig().getBoolean("settings.limit_natural_spawn");
        limitSpawnerSpawn = getConfig().getBoolean("settings.limit_spawner_spawn");
        debugLimitSpawn = getConfig().getBoolean("settings.debug.limit_spawn");
        debugLimits = getConfig().getBoolean("settings.debug.limits");
        debugChunkUnload = getConfig().getBoolean("settings.debug.chunk_unload");
        debugAgeCap = getConfig().getBoolean("settings.debug.age_cap");
        debugSpecial = getConfig().getBoolean("settings.debug.special");

        for (Map.Entry<String, Object> entry : this.getConfig().getValues(false).entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof Integer) {
                String name = entry.getKey().toLowerCase();
                int value = (Integer) entry.getValue();
                if (name.equals("agecapbaby")) {
                    ageCapBaby = value;
                } else if (name.equals("agecapbreed")) {
                    ageCapBreed = value;
                } else {
                    // A negative limit allows unlimited numbers of that mob.
                    if (value >= 0) {
                        limits.put(name, value);
                        if (debugLimits) {
                            getLogger().info(name + " limit: " + value);
                        }
                    }
                }
            }
        }

        if (getConfig().isList("settings.spawn_limited")) {
            for (String entityTypeName : getConfig().getStringList("settings.spawn_limited")) {
                EntityType entityType = EntityType.valueOf(entityTypeName.toUpperCase());
                if (entityType == null) {
                    getLogger().severe("Invalid entity type for spawn limiting: " + entityTypeName);
                } else {
                    spawnLimitedEntityTypes.add(entityType);
                }
            }
        }
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (Chunk c : getServer().getWorlds().get(0).getLoadedChunks()) {
            removeMobs(c);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(final ChunkUnloadEvent e) {
        removeMobs(e.getChunk());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawnEvent(final CreatureSpawnEvent event) {
        if ((event.getSpawnReason() == SpawnReason.BREEDING || event.getSpawnReason() == SpawnReason.EGG) && isFarmAnimal(event.getEntity())) {
            applyAgeCap((Animals) event.getEntity());
            for (Entity en : event.getEntity().getNearbyEntities(4, 4, 4)) {
                if (isFarmAnimal(en)) {
                    applyAgeCap((Animals) en);
                }
            }
        } else {
            // Villager breeding has a spawn reason of SpawnReason.DEFAULT
            // or SpawnReason.BREEDING depending on CraftBukkit version.
            boolean doLimitNaturalSpawn = limitNaturalSpawn &&
                                          (event.getSpawnReason() == SpawnReason.BREEDING ||
                                           event.getSpawnReason() == SpawnReason.NATURAL ||
                                          event.getSpawnReason() == SpawnReason.DEFAULT);
            boolean doLimitSpawnerSpawn = limitSpawnerSpawn && event.getSpawnReason() == SpawnReason.SPAWNER;
            if ((doLimitNaturalSpawn || doLimitSpawnerSpawn) &&
                spawnLimitedEntityTypes.contains(event.getEntityType()) && hasCap(event.getEntity())) {
                int cap = getCap(getCapKey(event.getEntity()));
                int count = 0;
                for (Entity otherEntity : event.getLocation().getChunk().getEntities()) {
                    if (otherEntity.getType() == event.getEntityType()) {
                        ++count;
                        if (count >= cap) {
                            break;
                        }
                    }
                }
                if (count >= cap) {
                    if (debugLimitSpawn) {
                        getLogger().info("Cancel spawn of " + getMobDescription(event.getEntity()) +
                                         " (reason = " + event.getSpawnReason().name().toLowerCase() + ", cap = " + cap + ")");
                    }

                    // See: https://github.com/NerdNu/NerdBugs/issues/180
                    // Trying remove() instead since we already use that.
                    event.getEntity().remove();
                }
            }
        }
    }

    public boolean isFarmAnimal(Entity en) {
        return (en instanceof Animals) && !(en instanceof Tameable);
    }

    public void applyAgeCap(Animals en) {
        if (ageCapBaby >= 0 && !en.isAdult()) {
            en.setAge(Math.max(en.getAge(), -ageCapBaby));
        } else if (ageCapBreed >= 0 && en.isAdult()) {
            en.setAge(Math.min(en.getAge(), ageCapBreed));
        }
        if (debugAgeCap) {
            getLogger().info("Age of " + getMobDescription(en) + " capped to " + en.getAge());
        }
    }

    /**
     * Return a String describing an entity's type (including color for sheep)
     * and location.
     * 
     * @param entity the entity.
     * @return the description, including location.
     */
    public String getMobDescription(Entity entity) {
        String key = getCapKey(entity);
        Location loc = entity.getLocation();
        return String.format("%s at (%s,%d,%d,%d)", key,
            loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Return the key string used to access the cap on a given mob type.
     * 
     * For sheep, the color is considered to be part of the type. Each sheep
     * color has its own distinct cap.
     * 
     * @param entity the entity.
     * @return the key used to access the limits map.
     */
    public String getCapKey(Entity entity) {
        StringBuilder key = new StringBuilder(entity.getType().name());
        if (entity instanceof Sheep) {
            key.append(((Sheep) entity).getColor().name());
        }
        return key.toString().toLowerCase();
    }

    /**
     * Return true if the specified Entity has a cap on numbers.
     * 
     * @param entity the entity.
     * @return true if the specified Entity has a cap on numbers.
     */
    public boolean hasCap(Entity entity) {
        return getCap(getCapKey(entity)) >= 0;
    }

    /**
     * Return the per chunk limit on entities with the specified cap key, or -1
     * if uncapped.
     * 
     * @param key the mob type as defined by getCapKey().
     * @return the per chunk cap for entities with that key, or -1.
     */
    public int getCap(String key) {
        Integer cap = limits.get(key);
        return (cap != null) ? cap : -1;
    }

    /**
     * Return true if the specified LivingEntity is special, meaning it has been
     * named, tamed, or is wearing items in its armor slots.
     * 
     * Such "special" mobs are exempted from being removed on chunk unload.
     * However, for hostiles, this is not a guarantee as the server can and will
     * despawn them without any event from Bukkit.
     * 
     * @return true if the mob is special and should be exempt from limits.
     */
    public boolean isSpecialMob(LivingEntity living) {
        if (living.getCustomName() != null) {
            return true;
        }

        if (living instanceof Tameable) {
            Tameable tameable = (Tameable) living;
            if (tameable.isTamed()) {
                return true;
            }
        }

        EntityEquipment equipment = living.getEquipment();
        for (ItemStack armor : equipment.getArmorContents()) {
            // Unarmored mobs, even animals, spawn with 1xAIR as armor.
            if (armor != null && armor.getType() != Material.AIR) {
                return true;
            }
        }

        return false;
    }

    public void removeMobs(Chunk chunk) {
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (Entity entity : chunk.getEntities()) {
            if (!entity.isDead() && (entity instanceof Animals || entity instanceof Monster)) {
                // Exempt "special mobs" from removal.
                // Animals and Monsters are both LivingEntitys so this cast
                // succeeds.
                boolean special = isSpecialMob((LivingEntity) entity);
                String key = getCapKey(entity);
                if (special) {
                    if (debugSpecial) {
                        getLogger().info("Special mob exempted from removal: " + getMobDescription(entity));
                    }
                } else {
                    Integer oldCount = count.get(key);
                    int mobCount = (oldCount == null) ? 1 : oldCount + 1;
                    count.put(key, mobCount);

                    int cap = getCap(key);
                    if (cap >= 0 && mobCount > cap) {
                        entity.remove();
                        if (debugChunkUnload) {
                            getLogger().info("Chunk unload removes " + getMobDescription(entity));
                        }
                    }
                }
            }
        }
    }
}
