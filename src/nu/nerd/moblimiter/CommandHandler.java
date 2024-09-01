package nu.nerd.moblimiter;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import nu.nerd.moblimiter.configuration.ConfiguredDefaults;
import nu.nerd.moblimiter.configuration.ConfiguredMob;
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

    // --------------------------------------------------------------------------------------------

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig(sender);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("count")) {
            countCommand(sender);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("limits")) {
            limitsCommand(sender, args);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("check")) {
            checkCommand(sender);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            helpText(sender, cmd, args);
        } else {
            commandsList(sender);
        }
        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Command to reload the configuration
     */
    private void reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("moblimiter.reload")) {
            sender.sendMessage(Component.text("You don't have permission to do this.")
                    .color(TextColor.fromHexString("#FF5555")));
            return;
        }
        plugin.getConfiguration().load();
        sender.sendMessage(Component.text("MobLimiter config reloaded!")
                .color(TextColor.fromHexString("#FFAA00")));
    }

    // --------------------------------------------------------------------------------------------

    /**
     * When a player runs /moblimiter with no arguments, list available commands
     * and guide newbies to /moblimiter help
     *
     * @param sender
     */
    private void commandsList(CommandSender sender) {

        Component helpMessage = LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&6&lMobLimiter: Available Commands&r");
        helpMessage = helpMessage.appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&6/moblimiter help&r - What is MobLimiter and how does it work?"));
        if (sender.hasPermission("moblimiter.limits")) {
            helpMessage = helpMessage.appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&6/moblimiter limits&r - List the limits for each mob type"));
        }
        if (sender.hasPermission("moblimiter.count")) {
            helpMessage = helpMessage.appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&6/moblimiter count&r - Count entities in the current chunk and radius"));
        }
        if (sender.hasPermission("moblimiter.check")) {
            helpMessage = helpMessage.appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&6/moblimiter check&r - Inspect limiting details for the mob you're looking at"));
        }
        if (sender.hasPermission("moblimiter.reload")) {
            helpMessage = helpMessage.appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&6/moblimiter reload&r - Reload configuration from disk"));
        }
        sender.sendMessage(helpMessage);
    }

    // --------------------------------------------------------------------------------------------

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
            Component helpMessage = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&6This server runs a plugin called &eMobLimiter" +
                            " &6in order to manage the amount of mobs on the server, as too many mobs creates lag" +
                            " which affects everyone. MobLimiter uses several methods to manage the mob population," +
                            " depending on how it is configured:").appendNewline();

            helpMessage = helpMessage.append(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&e1. Entity unload culling"))
                    .appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                            .deserialize("&6Mobs are culled down to a defined maximum" +
                                    " whenever a cluster of entities unloads or the server restarts."))
                    .appendNewline();

            helpMessage = helpMessage.append(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&e2. Real-time spawn limiting"))
                    .appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                            .deserialize("&6Mobs are tracked in real-time and new spawns" +
                                    " are prevented in an area if there are more than a certain amount" +
                                    " of that mob type there already."));

            sender.sendMessage(helpMessage);
        }
        if (num == 2) {

            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&e3. Age limiting").appendNewline()
                    .append(LegacyComponentSerializer.legacyAmpersand()
                            .deserialize("&6Mobs automatically die when they reach a certain age," +
                                    " dropping their items when they do. " +
                                    "e.g. a skeleton may live for 15 minutes before dying."))
                    .appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                            .deserialize("&6In all cases, \"special\" mobs are always protected" +
                                    " and will not be removed by the plugin. This includes mobs with" +
                                    " custom names, tamed animals, and mobs wearing armor."))
                    .appendNewline().append(LegacyComponentSerializer.legacyAmpersand()
                            .deserialize("To make up for the culling of passive farm animals," +
                                    " the plugin shortens the growth rate of baby animals and the" +
                                    " waiting time in between breeding events, to approximately &7" +
                                    (plugin.getConfiguration().getGrowthTicks() / 20) + " &6and &7" +
                                    (plugin.getConfiguration().getBreedingTicks() / 20) + " &6seconds " +
                                    "respectively.")));
        }
        sender.sendMessage(Component.text(String.format("[Page %d/2 - /moblimiter help #]", num)));
    }

    // --------------------------------------------------------------------------------------------

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
        Component countMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&6Entities in chunk: &r");

        sender.sendMessage(countMessager(chunkCounts, "&6Entities in chunk: &r"));
        sender.sendMessage(countMessager(radCounts, "&6Entities in radius: &r"));

    }

    // --------------------------------------------------------------------------------------------

    /**
     * Outputs the number of mobs nearby
     * @param map A hashmap of mob names and counts
     * @param header
     * @return
     */
    private Component countMessager(HashMap<String, Integer> map, String header) {
        Component countMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(header);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            countMessage = countMessage.append(Component.text(
                    "&f" +
                            entry.getKey().toLowerCase() +
                            ": &7" +
                            entry.getValue() +
                            " "));
        }
        return countMessage;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Command to display the configured limits
     */
    private void limitsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("moblimiter.limits")) return;
        List<String> lines = new ArrayList<>();
        ConfiguredDefaults d = plugin.getConfiguration().getDefaults();
        String values = String.format("Age: %d Max: %d Chunk: %d Cull: %d", d.getAge(),
                d.getMax(), d.getChunkMax(), d.getCull());
        lines.add(String.format("%s%s %s[%s]", "&9", "DEFAULT", "&e", values));
        TreeMap<String, ConfiguredMob> limits = new TreeMap<>(plugin.getConfiguration().getAllLimits());
        for (ConfiguredMob l : limits.values()) {
            values = String.format("Age: %d Max: %d Chunk: %d Cull: %d",
                    l.getAge(), l.getMax(), l.getChunkMax(), l.getCull());
            lines.add(String.format("%s%s %s[%s]", "&6", l.getKey(), "&e", values));
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
            int pages = ((lines.size() - 1) / 10) + 1;
            for (int i = offset; i <= offset + 9; i++) {
                if (i >= lines.size()) break;
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(lines.get(i)));
            }
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(String.format("%s[Page %d/%d]", "&7", page, pages)));
        } else {
            sender.sendMessage(Component.text("There are no limits configured. All mob types will fall back" +
                    " to the default block.").color(TextColor.fromHexString("#FF5555")));
        }
    }

    // --------------------------------------------------------------------------------------------

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

        sender.sendMessage(Component.text("---").color(TextColor.fromHexString("#FFAA00")));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                String.format("&7 (%d/%d nearby, %d/%d in chunk)", nearby, limits.getMax(), inChunk,
                        limits.getChunkMax())));

        String ageStr;
        if (limits.getAge() > -1) {
            if (!EntityHelper.isBreedingPair(entity)) {
                ageStr = String.format("%d/%d", plugin.getAgeLimiter().adjustedAge(entity), limits.getAge());
            } else {
                ageStr = "Breeding Pair Exempted";
            }
        } else {
            ageStr = "Not Limited";
        }

        String chunkCullStr = (limits.getCull() > -1 && isCullable) ? String.format("%d", limits.getCull()) : "Unlimited";

        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(String.format("Age: %s%s", "&7", ageStr)));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(String.format("Is Special: %s%b", "&7", isSpecial)));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(String.format("Chunk Unload Cap: %s%s", "&7", chunkCullStr)));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(String.format("More Can Spawn: %s%b", "&7", moreCanSpawn)));

        sender.sendMessage(Component.text("---").color(TextColor.fromHexString("#FFAA00")));

    }


}
