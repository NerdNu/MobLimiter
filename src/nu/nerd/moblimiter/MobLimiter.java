package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

	/**
	 * Map from lower case EntityType.name() to max number of entities of that
	 * type. Note that EntityType.name() is different from EntityType.getName().
	 * The former is MUSHROOM_COW while the latter is MushroomCow.
	 */
	public Map<String, Integer> limits = new HashMap<String, Integer>();
	public int ageCapBaby = -1;
	public int ageCapBreed = -1;

	/**
	 * If true, per-chunk mob caps are enforced for non-farm-animal mobs when
	 * they spawn, in addition to when the chunk is unloaded.
	 */
	public boolean limitNaturalSpawn;

	/**
	 * If true, the mob cap applies even when the mob was spawned from a
	 * mob-spawner.
	 */
	public boolean limitSpawnerSpawn;

	/**
	 * If true, cancelled spawns are shown in the server log.
	 */
	public boolean debugLimitSpawn;

	/**
	 * The set of Entity types whose caps are enforced when spawning.
	 * 
	 * Note that farm animals are explicitly excluded from that cap. Players can
	 * breed them over the limit.
	 */
	public HashSet<EntityType> spawnLimitedEntityTypes = new HashSet<EntityType>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		for (Map.Entry<String, Object> entry : this.getConfig().getValues(false).entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof Integer) {
				if (entry.getKey().equals("agecapbaby")) {
					ageCapBaby = (Integer) entry.getValue();
				} else if (entry.getKey().equals("agecapbreed")) {
					ageCapBreed = (Integer) entry.getValue();
				} else {
					// A negative limit allows unlimited numbers of that mob.
					int max = (Integer) entry.getValue();
					if (max >= 0) {
						limits.put(entry.getKey().toLowerCase(), max);
					}
				}
			}
		}

		limitNaturalSpawn = getConfig().getBoolean("settings.limit_natural_spawn");
		limitSpawnerSpawn = getConfig().getBoolean("settings.limit_spawner_spawn");
		debugLimitSpawn = getConfig().getBoolean("settings.debug.limit_spawn");

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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
				spawnLimitedEntityTypes.contains(event.getEntityType()) && hasCap(event.getEntityType())) {
				int cap = getCap(event.getEntityType());
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
						Location loc = event.getLocation();
						String message = String.format("Cancel spawn of %s at (%s,%d,%d,%d) (reason = %s, cap = %d)",
							event.getEntityType().name(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
							event.getSpawnReason().name().toLowerCase(), cap);
						getLogger().info(message);
					}
					event.setCancelled(true);
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
	}

	/**
	 * Return true if the specified Entity type has a cap on numbers.
	 * 
	 * @return true if the specified Entity type has a cap on numbers.
	 */
	public boolean hasCap(EntityType type) {
		return getCap(type) >= 0;
	}

	/**
	 * Return true if the specified Entity type has a cap on numbers, or -1 if
	 * uncapped.
	 * 
	 * @return true if the specified Entity type has a cap on numbers, or -1 if
	 *         uncapped.
	 */
	public int getCap(EntityType type) {
		Integer cap = limits.get(type.name().toLowerCase());
		return (cap != null) ? cap : -1;
	}

	public void removeMobs(Chunk chunk) {
		Map<String, Integer> count = new HashMap<String, Integer>();
		for (Entity entity : chunk.getEntities()) {
			if (!entity.isDead() && (entity instanceof Animals || entity instanceof Monster)) {
				String mobName = entity.getType().name().toLowerCase();
				if (entity instanceof Sheep) {
					mobName += ((Sheep) entity).getColor().name().toLowerCase();
				}
				if (count.get(mobName) == null) {
					count.put(mobName, 0);
				}
				int mobCount = count.get(mobName);
				count.put(mobName, ++mobCount);
				if (limits.get(mobName) != null) {
					if (mobCount > limits.get(mobName)) {
						entity.remove();
					}
				}
			}
		}
	}
}
