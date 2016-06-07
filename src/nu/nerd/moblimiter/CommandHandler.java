package nu.nerd.moblimiter;


import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler implements CommandExecutor {


    private MobLimiter plugin;


    public CommandHandler() {
        plugin = MobLimiter.instance;
        plugin.getCommand("moblimiter").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig(sender);
        }
        else if (args.length > 0 && args[0].equalsIgnoreCase("count")) {
            countCommand(sender);
        }
        else if (args.length > 0 && args[0].equalsIgnoreCase("limits")) {
            limitsCommand(sender, args);
        }
        else {
            infoText(sender, cmd, args);
        }
        return true;
    }


    /**
     * Command to reload the configuration
     */
    private void reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("moblimiter.reload")) return;
        plugin.getConfiguration().load();
        sender.sendMessage(ChatColor.GOLD + "MobLimiter config reloaded");
    }


    /**
     * When a player runs /moblimiter, print help text
     */
    private void infoText(CommandSender sender, Command cmd, String[] args) {

        int num = 1;
        if (args.length > 0) {
            try {
                num = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                num = 1;
            }
        }

        if (num == 1) {
            StringBuilder msg = new StringBuilder();
            msg.append(ChatColor.GOLD);
            msg.append("This server runs a plugin called " + ChatColor.YELLOW + "MobLimiter"
                    + ChatColor.GOLD + " in order to manage the "
                    + "amount of mobs on the server, as too many mobs creates lag which affects "
                    + "everyone. MobLimiter uses several methods to manage the mob population, depending on how" +
                    " it is configured:\n");

            msg.append(ChatColor.YELLOW);
            msg.append("1. Chunk unload culling\n");
            msg.append(ChatColor.GOLD);
            msg.append("Mobs are culled down to a defined maximum whenever a chunk unloads or the server restarts.\n");

            msg.append(ChatColor.YELLOW);
            msg.append("2. Real-time spawn limiting\n");
            msg.append(ChatColor.GOLD);
            msg.append("Mobs are tracked in real-time and new spawns are prevented in an area if there are more than a " +
                    "certain amount of that mob type there already.\n");
            sender.sendMessage(msg.toString());
        }

        if (num == 2) {
            StringBuilder msg = new StringBuilder();
            msg.append(ChatColor.YELLOW);
            msg.append("3. Age limiting\n");
            msg.append(ChatColor.GOLD);
            msg.append("Mobs automatically die when they reach a certain age, dropping their items when they do. " +
                    "e.g. a skeleton may live for 15 minutes before dying.\n");

            msg.append("In all cases, \"special\" mobs are always protected and will not be removed by the plugin. This " +
                    "includes mobs with custom names, tamed animals, and mobs wearing armor.\n");

            msg.append("To make up for the culling of passive farm animals, the plugin shortens the growth rate of " +
                    "baby animals and the waiting time in between breeding events, to approximately ");
            msg.append(ChatColor.GRAY);
            msg.append(plugin.getConfiguration().getGrowthTicks() / 20);
            msg.append(ChatColor.GOLD);
            msg.append(" and ");
            msg.append(ChatColor.GRAY);
            msg.append(plugin.getConfiguration().getBreedingTicks() / 20);
            msg.append(ChatColor.GOLD);
            msg.append(" seconds respectively.");
            sender.sendMessage(msg.toString());
        }

        sender.sendMessage(String.format("[Page %d/2 - /moblimiter #]", num));

    }


    /**
     * Command to count entities in the chunk and radius
     */
    private void countCommand(CommandSender sender) {

        if (!sender.hasPermission("moblimiter.count")) return;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console can't do that.");
            return;
        }
        Player player = (Player) sender;

        // Count mobs in the chunk
        HashMap<String, Integer> chunkCounts = new HashMap<String, Integer>();
        for (Entity e : player.getLocation().getChunk().getEntities()) {
            if (chunkCounts.containsKey(e.getType().toString())) {
                int count = chunkCounts.get(e.getType().toString()) + 1;
                chunkCounts.put(e.getType().toString(), count);
            } else {
                chunkCounts.put(e.getType().toString(), 1);
            }
        }

        // Count mobs in the radius
        HashMap<String, Integer> radCounts = new HashMap<String, Integer>();
        int radius = plugin.getConfiguration().getRadius();
        World world = player.getWorld();
        Chunk start = player.getLocation().getChunk();
        for (int x = start.getX() - radius; x <= start.getX() + radius; x++) {
            for (int z = start.getZ() - radius; z <= start.getZ() + radius; z++) {
                Chunk c = world.getChunkAt(x, z);
                for (Entity e : c.getEntities()) {
                    if (radCounts.containsKey(e.getType().toString())) {
                        int count = radCounts.get(e.getType().toString()) + 1;
                        radCounts.put(e.getType().toString(), count);
                    } else {
                        radCounts.put(e.getType().toString(), 1);
                    }
                }
            }
        }

        // Print results
        StringBuilder sb = new StringBuilder(ChatColor.GOLD + "Entities in chunk: ");
        for (Map.Entry<String, Integer> entry : chunkCounts.entrySet()) {
            sb.append(ChatColor.WHITE);
            sb.append(entry.getKey().toLowerCase());
            sb.append(": ");
            sb.append(ChatColor.GRAY);
            sb.append(entry.getValue());
            sb.append(" ");
        }
        sender.sendMessage(sb.toString());
        sb = new StringBuilder(ChatColor.GOLD + "Entities in radius: ");
        for (Map.Entry<String, Integer> entry : radCounts.entrySet()) {
            sb.append(ChatColor.WHITE);
            sb.append(entry.getKey().toLowerCase());
            sb.append(": ");
            sb.append(ChatColor.GRAY);
            sb.append(entry.getValue());
            sb.append(" ");
        }
        sender.sendMessage(sb.toString());

    }


    /**
     * Command to display the configured limits
     */
    private void limitsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("moblimiter.limits")) return;
        List<String> lines = new ArrayList<String>();
        for (ConfiguredMob l : plugin.getConfiguration().getAllLimits().values()) {
            String values = String.format("Age: %d Max: %d Chunk: %d Cull: %d", l.getAge(), l.getMax(), l.getChunkMax(), l.getCull());
            lines.add(String.format("%s%s %s[%s]", ChatColor.GOLD, l.getType().toString(), ChatColor.YELLOW, values));
        }
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                page = 1;
            }
        }
        if (lines.size() > 0) {
            int offset = (page - 1) * 10;
            for (int i = offset; i <= offset + 9; i++) {
                if (i >= lines.size()) break;
                sender.sendMessage(lines.get(i));
            }
            sender.sendMessage(String.format("%s[Page %d/2]", ChatColor.GRAY, page));
        } else {
            sender.sendMessage("There are no limits configured. All mob types will fall back to the default block.");
        }
    }


}
