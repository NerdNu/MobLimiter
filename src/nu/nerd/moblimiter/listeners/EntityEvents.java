package nu.nerd.moblimiter.listeners;

import net.kyori.adventure.text.Component;
import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public class EntityEvents implements Listener {

    /**
     * If a tadpole tries to grow up, cancel it and revert its age back.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTransformEvent(EntityTransformEvent event) {
        Entity entity = event.getEntity();
        if(entity.getType().equals(EntityType.TADPOLE)) {
            if(entity.customName() != null) event.setCancelled(true);
            if(entity instanceof Tadpole) {
                Tadpole tadpole = (Tadpole) entity;
                tadpole.setAge(Integer.MIN_VALUE);
            }
        }
    }

    /**
     * Logs mob deaths.
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player player = event.getEntity().getKiller();

        if(player == null || !(entity instanceof Breedable) || !(entity instanceof Tadpole)) {
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

            MobLimiter.instance.getComponentLogger().info(Component.text(String.format("%s killed %s named %s at %s %s",
                    player.getName(),
                    breedable.getType(),
                    breedable.customName(),
                    MobLimiter.locationToString(breedable.getLocation()),
                    extrainfo)));

        }

    }

}
