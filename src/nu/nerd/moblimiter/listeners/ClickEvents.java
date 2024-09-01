package nu.nerd.moblimiter.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ClickEvents implements Listener {

    MobLimiter plugin;
    private List<Material> spawnEggs;

    public ClickEvents() {
        plugin = MobLimiter.instance;
        spawnEggs = MobLimiter.instance.getConfiguration().getSpawnEggs();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Formerly part of LimitSpawnEggs
     * Stops players from changing the mob of a spawner with spawn eggs
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("moblimiter.spawners.bypass")) {
            return;
        }
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK &&
        event.getClickedBlock().getType() == Material.SPAWNER &&
        event.getItem() != null &&
        spawnEggs.contains(event.getItem().getType())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You don't have permission to edit spawners.")
                    .color(TextColor.fromHexString("#FF5555")));
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Formerly part of KeepBabyMobs
     * Handles the locking of baby mobs. Originally handled by KeepBabyMobs.
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

    // --------------------------------------------------------------------------------------------

    /**
     * Locks the mob's age and makes it a baby forever
     * @param entity The entity being age locked
     * @param player The player initiating the age lock
     * @param hand The item being used to lock the entity's age
     */
    public void lockMobAge(Entity entity, Player player, ItemStack hand) {
        // If the held item is a nametag
            if(hand.getItemMeta().hasDisplayName()) {
                // Tadpoles are checked separately due to a different handling of their growth.
                if(entity.getType().equals(EntityType.TADPOLE)) {
                    ((Tadpole) entity).setAgeLock(true);
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

    // --------------------------------------------------------------------------------------------

    /**
     * Logs any locked mobs to console.
     * @param player The player locking the mob
     * @param entity The entity being locked
     */
    public void logLock(Player player, Entity entity) {
        player.sendMessage(Component.text("That mob has now been age locked. How adorable!")
                .color(TextColor.fromHexString("#FFAA00")));
        MobLimiter.instance.getComponentLogger().info(Component.text(String.format("%s age locked %s named %s at %s",
                player.getName(), entity.getType(), entity.customName(),
                MobLimiter.locationToString(entity.getLocation()))));
    }

}
