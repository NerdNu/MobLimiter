package nu.nerd.moblimiter.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import nu.nerd.moblimiter.MobLimiter;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
}
