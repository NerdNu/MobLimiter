package nu.nerd.moblimiter;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
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

	public Map<String, Integer> limits = new HashMap<String, Integer>();
	public int ageCapBaby = -1;
	public int ageCapBreed = -1;

	@Override
	public void onEnable() {
		try {
			saveDefaultConfig();
		} catch (Exception ex) {
		}
		this.getConfig().options().copyDefaults(true);
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
						limits.put(entry.getKey(), max);
					}
				}
			}
		}
		this.saveConfig();
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
	public void onCreatureSpawnEvent(final CreatureSpawnEvent e) {
		if ((e.getSpawnReason() == SpawnReason.BREEDING || e.getSpawnReason() == SpawnReason.EGG) && isFarmAnimal(e.getEntity())) {
			applyAgeCap((Animals) e.getEntity());
			for (Entity en : e.getEntity().getNearbyEntities(4, 4, 4)) {
				if (isFarmAnimal(en)) {
					applyAgeCap((Animals) en);
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
