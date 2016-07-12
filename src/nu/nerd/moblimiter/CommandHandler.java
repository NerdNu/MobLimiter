package nu.nerd.moblimiter;


import nu.nerd.moblimiter.configuration.ConfiguredDefaults;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import java.util.*;

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
        else if (args.length > 0 && args[0].equalsIgnoreCase("check")) {
            checkCommand(sender);
        }
        else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            helpText(sender, cmd, args);
        }
        else {
            commandsList(sender);
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
     * When a player runs /moblimiter with no arguments, list available commands
     * and guide newbies to /moblimiter help
     * @param sender
     */
    private void commandsList(CommandSender sender) {
        StringBuilder msg = new StringBuilder();
        msg.append("&6&lMobLimiter: Available Commands\n");
        msg.append("&6/moblimiter help&r - What is MobLimiter and how does it work?\n");
        if (sender.hasPermission("moblimiter.limits")) {
            msg.append("&6/moblimiter limits&r - List the limits for each mob type\n");
        }
        if (sender.hasPermission("moblimiter.count")) {
            msg.append("&6/moblimiter count&r - Count entities in the current chunk and radius\n");
        }
        if (sender.hasPermission("moblimiter.check")) {
            msg.append("&6/moblimiter check&r - Inspect limiting details for the mob you're looking at\n");
        }
        if (sender.hasPermission("moblimiter.reload")) {
            msg.append("&6/moblimiter reload&r - Reload configuration from disk\n");
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.toString()));
    }


    /**
     * When a player runs /moblimiter help, print help text
     */
    private void helpText(CommandSender sender, Command cmd, String[] args) {

        int num = 1;
        if (args.length > 1) {
            try {
                num = Integer.parseInt(args[1]);
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

        sender.sendMessage(String.format("[Page %d/2 - /moblimiter help #]", num));

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
        Chunk playerChunk = player.getLocation().getChunk();
        int chunkRadius = plugin.getConfiguration().getRadius();

        // Count mobs in the chunk
        HashMap<String, Integer> chunkCounts = EntityHelper.summarizeMobsInChunk(playerChunk);

        // Count mobs in the radius
        HashMap<String, Integer> radCounts = EntityHelper.summarizeMobsInRadius(playerChunk, chunkRadius);

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
        ConfiguredDefaults d = plugin.getConfiguration().getDefaults();
        String values = String.format("Age: %d Max: %d Chunk: %d Cull: %d", d.getAge(), d.getMax(), d.getChunkMax(), d.getCull());
        lines.add(String.format("%s%s %s[%s]", ChatColor.GOLD, "DEFAULT", ChatColor.YELLOW, values));
        TreeMap<String, ConfiguredMob> limits = new TreeMap<String, ConfiguredMob>(plugin.getConfiguration().getAllLimits());
        for (ConfiguredMob l : limits.values()) {
            values = String.format("Age: %d Max: %d Chunk: %d Cull: %d", l.getAge(), l.getMax(), l.getChunkMax(), l.getCull());
            lines.add(String.format("%s%s %s[%s]", ChatColor.GOLD, l.getKey(), ChatColor.YELLOW, values));
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
            int pages = ((lines.size()-1) / 10) + 1;
            for (int i = offset; i <= offset + 9; i++) {
                if (i >= lines.size()) break;
                sender.sendMessage(lines.get(i));
            }
            sender.sendMessage(String.format("%s[Page %d/%d]", ChatColor.GRAY, page, pages));
        } else {
            sender.sendMessage("There are no limits configured. All mob types will fall back to the default block.");
        }
    }


    /**
     * Command to inspect details about the mob the player is looking at
     */
    private void checkCommand(CommandSender sender) {

        if (!sender.hasPermission("moblimiter.check")) return;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console can't do that.");
            return;
        }

        Player player = (Player) sender;
        LivingEntity entity = EntityHelper.getMobInLineOfSight(player);
        if (entity == null || entity.isDead() || !EntityHelper.isLimitableMob(entity)) {
            sender.sendMessage("No mob in sight");
            return;
        }

        ConfiguredMob limits = plugin.getConfiguration().getLimits(entity);
        Chunk chunk = entity.getLocation().getChunk();
        int chunkRadius = plugin.getConfiguration().getRadius();
        HashMap<String, Integer> chunkCounts = EntityHelper.summarizeMobsInChunk(chunk);
        HashMap<String, Integer> radCounts = EntityHelper.summarizeMobsInRadius(chunk, chunkRadius);
        int nearby = radCounts.get(limits.getKey());
        int inChunk = chunkCounts.get(limits.getKey());
        boolean isSpecial = EntityHelper.isSpecialMob(entity);
        boolean isCullable = !entity.isDead() && (entity instanceof Animals || entity instanceof Monster);
        boolean canSpawnChunk = (limits.getChunkMax() > inChunk || limits.getChunkMax() < 0);
        boolean canSpawnNearby = (limits.getMax() > nearby || limits.getMax() < 0);
        boolean moreCanSpawn = canSpawnChunk && canSpawnNearby;

        sender.sendMessage(ChatColor.GOLD + "---");

        StringBuilder title = new StringBuilder(ChatColor.GOLD + limits.getKey());
        title.append(ChatColor.GRAY);
        title.append(String.format(" (%d/%d nearby, %d/%d in chunk)", nearby, limits.getMax(), inChunk, limits.getChunkMax()));
        sender.sendMessage(title.toString());

        String ageStr;
        if (limits.getAge() > -1) {
            if (!EntityHelper.isBreedingPair(entity)) {
                ageStr = String.format("%d/%d", entity.getTicksLived(), limits.getAge());
            } else {
                ageStr = "Breeding Pair Exempted";
            }
        } else {
            ageStr = "Not Limited";
        }

        String chunkCullStr = (limits.getCull() > -1 && isCullable) ? String.format("%d", limits.getCull()) : "Unlimited";

        sender.sendMessage(String.format("Age: %s%s", ChatColor.GRAY, ageStr));
        sender.sendMessage(String.format("Is Special: %s%b", ChatColor.GRAY, isSpecial));
        sender.sendMessage(String.format("Chunk Unload Cap: %s%s", ChatColor.GRAY, chunkCullStr));
        sender.sendMessage(String.format("More Can Spawn: %s%b", ChatColor.GRAY, moreCanSpawn));

        sender.sendMessage(ChatColor.GOLD + "---");

    }


}
