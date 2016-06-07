package nu.nerd.moblimiter;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

public class CommandHandler implements CommandExecutor {


    private MobLimiter plugin;


    public CommandHandler() {
        plugin = MobLimiter.instance;
        plugin.getCommand("moblimiter").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        infoText(sender, cmd, args);
        return true;
    }


    private void infoText(CommandSender sender, Command cmd, String[] args) {

        int num = 1;
        if (args.length > 1) {
            try {
                num = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                num = 1;
            }
        }

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

        int length = ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH;
        int height = ChatPaginator.OPEN_CHAT_PAGE_HEIGHT - 1;
        ChatPaginator.ChatPage page = ChatPaginator.paginate(msg.toString(), num, length, height);
        sender.sendMessage(page.getLines());
        sender.sendMessage(String.format("[Page %d/%d - /moblimiter #]", page.getPageNumber(), page.getTotalPages()));

    }


}
