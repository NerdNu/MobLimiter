package nu.nerd.moblimiter.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityEvents implements Listener {

    MobLimiter plugin;

    public EntityEvents() {
        plugin = MobLimiter.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Formerly part of KeepBabyMobs.
     * Logs the death of an entity.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player player = event.getEntity().getKiller();

        if(player == null || !(entity instanceof Breedable)) {
            if(entity instanceof Tadpole) {
                logDeath(player, entity, "");
            }
            return;
        }

        Breedable breedable = (Breedable) entity;

        if(breedable.getAgeLock()) {
            String extrainfo = "";
            if(entity instanceof Cat) {
                extrainfo = "type: " + ((Cat) entity).getCatType().name();
            }
            if(entity instanceof Tameable) {
                Tameable tameable = ((Tameable) entity);
                if(tameable.isTamed()) {
                    extrainfo = "owner: " + ((Tameable) entity).getOwner().getName();
                }
            }

            logDeath(player, breedable, extrainfo);

        }

    }

    // --------------------------------------------------------------------------------------------

    /**
     * Outputs the death details of an age locked baby animal
     * @param player The player who killed the animal
     * @param entity The mob killed
     * @param extrainfo Extra information like coat colour and owner
     */
    public void logDeath(Player player, Entity entity, String extrainfo) {
        MobLimiter.instance.getComponentLogger().info(Component.text(String.format("%s killed %s named %s at %s %s",
                player.getName(),
                entity.getType(),
                PlainTextComponentSerializer.plainText().serialize(entity.customName()),
                MobLimiter.locationToString(entity.getLocation()),
                extrainfo)));
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Apply the breeding changes when a new animal is bred
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawnEvent(final CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING || reason == CreatureSpawnEvent.SpawnReason.EGG || reason == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG)) {
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

    // --------------------------------------------------------------------------------------------

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

    // --------------------------------------------------------------------------------------------

    /**
     * Whether the entity in question is a farm animal or not
     * @param entity the entity to check
     * @return true if this is a farm animal
     */
    private boolean isFarmAnimal(Entity entity) {
        return (entity instanceof Animals) && !(entity instanceof Tameable);
    }

}
