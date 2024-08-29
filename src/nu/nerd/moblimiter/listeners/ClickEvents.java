package nu.nerd.moblimiter.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ClickEvents implements Listener {

    /**
     * Handles the locking of baby mobs. Originally handled by KeepBabyMobs.
     * @param event The entity interact event
     */
    @EventHandler
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack hand = player.getEquipment().getItemInMainHand();

        if(hand.getType().equals(Material.NAME_TAG)) {
            lockMobAge(entity, player, hand);
        }

    }

    public void lockMobAge(Entity entity, Player player, ItemStack hand) {
        // If the held item is a nametag
            if(hand.getItemMeta().hasDisplayName()) {
                // Tadpoles are checked separately due to a different handling of their growth.
                if(entity.getType().equals(EntityType.TADPOLE)) {
                    logLock(player, entity);

                    // Check other mobs that can age and breed after.
                } else if(entity instanceof Breedable) {
                    Breedable breedable = (Breedable) entity;

                    if(!breedable.isAdult()) {
                        breedable.setAgeLock(true);

                        if(!(breedable instanceof Horse)) {
                            breedable.setAge(Integer.MIN_VALUE);

                        }
                        logLock(player, breedable);
                    }

                }
            }
    }

    /**
     * Logs any locked mobs to console.
     * @param player The player locking the mob
     * @param entity The entity being locked
     */
    public void logLock(Player player, Entity entity) {
        player.sendMessage(Component.text("That mob has now been age locked. How adorable!").color(TextColor.fromHexString("#FFAA00")));
        MobLimiter.instance.getComponentLogger().info(Component.text(String.format("%s age locked %s named %s at %s",
                player.getName(), entity.getType(), entity.customName(), MobLimiter.locationToString(entity.getLocation()))));
    }

}
