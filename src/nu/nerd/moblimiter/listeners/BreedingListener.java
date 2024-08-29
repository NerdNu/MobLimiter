package nu.nerd.moblimiter.listeners;

import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;


/**
 * Handle faster breeding
 */
public class BreedingListener implements Listener {


    private MobLimiter plugin;


    public BreedingListener() {
        plugin = MobLimiter.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    /**
     * Apply the breeding changes when a new animal is bred
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawnEvent(final CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if ((reason == SpawnReason.BREEDING || reason == SpawnReason.EGG || reason == SpawnReason.DISPENSE_EGG)) {
            if (isFarmAnimal(event.getEntity())) {
                applyBreedingChanges((Animals) event.getEntity());
                for (Entity en : event.getEntity().getNearbyEntities(4, 4, 4)) {
                    if (isFarmAnimal(en)) {
                        applyBreedingChanges((Animals) en);
                    }
                }
            }
        }
    }


    /**
     * Controls the rate farm animals grow up and the length of their breeding cooldown.
     * growthTicks: How many ticks until the animal becomes an adult.
     * breedingTicks: How many ticks until the animal can breed again.
     * A value of zero makes the condition instant, and -1 disables tampering with vanilla behavior.
     * This functionality was called "agecapbaby" and "agecapbaby" respectively in MobLimiter 1.x.
     * @param animal the animal to apply the changes to
     */
    private void applyBreedingChanges(Animals animal) {
        if (animal.getAgeLock()) return;
        int growthTicks = plugin.getConfiguration().getGrowthTicks();
        int breedingTicks = plugin.getConfiguration().getBreedingTicks();
        if (growthTicks > -1 && !animal.isAdult()) {
            animal.setAge(Math.max(animal.getAge(), -growthTicks));
        } else if (breedingTicks > -1 && animal.isAdult()) {
            animal.setAge(Math.min(animal.getAge(), breedingTicks));
        }
    }


    /**
     * Whether the entity in question is a farm animal or not
     * @param entity the entity to check
     * @return true if this is a farm animal
     */
    private boolean isFarmAnimal(Entity entity) {
        return (entity instanceof Animals) && !(entity instanceof Tameable);
    }


}
