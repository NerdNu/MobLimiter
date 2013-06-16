package nu.nerd.moblimiter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_5_R3.CraftChunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MobLimiter extends JavaPlugin implements Listener {

	public Map<String, Integer> limits = new HashMap<String, Integer>();
	public Map<EntityType, String> animalPlurals = new HashMap<EntityType, String>();
	public Map<EntityType, String> animalSingulars = new HashMap<EntityType, String>();

	public int ageCapBaby = -1;
	public int ageCapBreed = -1;

	public static int numberControlRadiusSquared;
	public static int maxOneType;
	public static int maxAnyType;

	public Map<String, Long> lastSpamMessage = new HashMap<String, Long>();
	public String breedLimitOneAnimalMessage;
	public String breedLimitAllAnimalsMessage;

	@Override
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		for (Map.Entry<String, Object> entry : this.getConfig().getValues(false).entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof Integer) {
				if (entry.getKey().equals("agecapbaby")) {
					ageCapBaby = (Integer) entry.getValue();
				} else if (entry.getKey().equals("agecapbreed")) {
					ageCapBreed = (Integer) entry.getValue();
				} else {
					if (((Integer) entry.getValue()) >= 0)
						limits.put(entry.getKey(), (Integer) entry.getValue());
				}
			}
		}

		numberControlRadiusSquared = this.getConfig().getInt("AnimalLimit.RadiusSquared", 40000);
		maxOneType = this.getConfig().getInt("AnimalLimit.MaxOneType", 80);
		maxAnyType = this.getConfig().getInt("AnimalLimit.MaxAllTypes", 320);

		breedLimitOneAnimalMessage = this.getConfig().getString("Messages.BreedLimitOneAnimal", "Admin of this server is so lazy he forgot to set config!");
		breedLimitAllAnimalsMessage = this.getConfig().getString("Messages.BreedLimitAllAnimals", "Admin of this server is so lazy he forgot to set config!");


		this.saveConfig();

		animalPlurals.put(EntityType.CHICKEN, "Chickens");
		animalPlurals.put(EntityType.COW, "Cows");
		animalPlurals.put(EntityType.MUSHROOM_COW, "Mooshrooms");
		animalPlurals.put(EntityType.PIG, "Pigs");
		animalPlurals.put(EntityType.SHEEP, "Sheep");
		animalSingulars.put(EntityType.CHICKEN, "Chicken");
		animalSingulars.put(EntityType.COW, "Cow");
		animalSingulars.put(EntityType.MUSHROOM_COW, "Mooshroom");
		animalSingulars.put(EntityType.PIG, "Pig");
		animalSingulars.put(EntityType.SHEEP, "Sheep");

		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		for (Chunk c : getServer().getWorlds().get(0).getLoadedChunks()) {
			removeMobs(c);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChunkUnload(final ChunkUnloadEvent e) {
		removeMobs(e.getChunk());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCreatureSpawnEvent(final CreatureSpawnEvent e) {
		if (isFarmAnimal(e.getEntity()))
		{
			if (canBreed(e.getLocation(), e.getEntityType()) != 0)
			{
				e.setCancelled(true);
				return;
			}

			if (e.getSpawnReason() == SpawnReason.BREEDING) {
				applyAgeCap((Animals) e.getEntity());
				for (Entity en : e.getEntity().getNearbyEntities(4, 4, 4)) {
					if (isFarmAnimal(en)) {
						applyAgeCap((Animals) en);
					}
				}
			}
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onEntityPortal(final EntityPortalEvent e)
	{
		if (isFarmAnimal(e.getEntity()) && canBreed(e.getTo(), e.getEntityType()) != 0)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent e)
	{
		Entity ent = e.getRightClicked();
		if (ent == null || !isFarmAnimal(ent))
			return;

		Player player = e.getPlayer();
		ItemStack hand = player.getItemInHand();

		if (hand == null || !isBreedingFood(ent.getType(), hand.getType()))
			return;

		int breedPossibility = canBreed(ent.getLocation(), ent.getType());
		if (breedPossibility != 0)
		{
			Long lastMessage = lastSpamMessage.get(player.getName());
			if (lastMessage == null || System.currentTimeMillis() - lastMessage > 5000)
			{
				String message = null;
				if (breedPossibility == -1)
				{
					message = breedLimitAllAnimalsMessage;
					message = message.replace("<AnimalsLimit>", Integer.toString(maxAnyType));
				}
				else
				{
					message = breedLimitOneAnimalMessage;
					message = message.replace("<AnimalTypeLimit>", Integer.toString(maxOneType));
					message = message.replace("<AnimalPlural>", animalPlurals.get(ent.getType()));
				}

				message = message.replace("<Animal>", animalSingulars.get(ent.getType()));
				message = message.replace("<MaxAnimalRadius>", Integer.toString((int) Math.round(Math.sqrt(numberControlRadiusSquared))));

				Message(message, player);
				lastSpamMessage.put(player.getName(), System.currentTimeMillis());
			}
			
			e.setCancelled(true);
			return;

		}
	}
	public boolean isFarmAnimal(Entity en) {
		return (en instanceof Animals) && !(en instanceof Tameable);
	}

	public void applyAgeCap(Animals en) {
		if (ageCapBaby >= 0 && !en.isAdult()) {
			en.setAge(Math.max(en.getAge(), -ageCapBaby));
		} else if (ageCapBreed >= 0 && en.isAdult()) {
			en.setAge(Math.min(en.getAge(), ageCapBreed));
		}
	}

	public void removeMobs(Chunk c) {
		net.minecraft.server.v1_5_R3.Chunk chunk = ((CraftChunk) c).getHandle();
		Map<String, Integer> count = new HashMap<String, Integer>();
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < chunk.entitySlices[i].size(); j++) {
				Object obj = chunk.entitySlices[i].get(j);
				Entity entity = null;
				if (obj instanceof net.minecraft.server.v1_5_R3.Entity) {
					entity = ((net.minecraft.server.v1_5_R3.Entity) obj).getBukkitEntity();
				} else {
					entity = (Entity) obj;
				}
				if (entity != null) {
					if (entity instanceof Creature) {
						String mobname = entity.getType().name().toLowerCase();

						if (count.get(mobname) == null) {
							count.put(mobname, 0);
						}

						int mbcount = count.get(mobname);
						count.put(mobname, ++mbcount);
						if (limits.get(mobname) != null) {
							if (mbcount > limits.get(mobname)) {
								((LivingEntity) entity).remove();
							}
						}

						if (!entity.isDead() && entity instanceof Sheep)
						{
							mobname += ((Sheep) entity).getColor().name().toLowerCase();

							if (count.get(mobname) == null) {
								count.put(mobname, 0);
							}

							mbcount = count.get(mobname);
							count.put(mobname, ++mbcount);
							if (limits.get(mobname) != null) {
								if (mbcount > limits.get(mobname)) {
									((LivingEntity) entity).remove();
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean isBreedingFood(EntityType type, Material food)
	{
		switch (type)
		{
		case SHEEP:
		case COW:
			return food == Material.WHEAT;
		case PIG:
			return food == Material.CARROT_ITEM;
		case CHICKEN:
			return food == Material.SEEDS || food == Material.PUMPKIN_SEEDS || food == Material.NETHER_STALK || food == Material.MELON_SEEDS;
		default:
			return false;
		}
	}

	public int canBreed(Location location, EntityType type)
	{		
		int numberOfOneType = 0;
		int numberOfAllTypes = 0;

		Collection<Animals> animals = location.getWorld().getEntitiesByClass(Animals.class);

		for (Animals animal : animals)
		{
			if (!isFarmAnimal(animal))
				continue;

			double distance = animal.getLocation().distanceSquared(location);

			if (distance <= numberControlRadiusSquared)
			{
				if (++numberOfAllTypes >= maxAnyType)
					return -1;

				if (type == animal.getType() && ++numberOfOneType >= maxOneType)
					return -2;
			}
		}

		return 0;
	}

	public static void Message(String message, CommandSender sender)
	{
		message = message.replaceAll("\\&([0-9abcdefklmnor])", ChatColor.COLOR_CHAR + "$1");

		final String newLine = "\\[NEWLINE\\]";
		String[] lines = message.split(newLine);

		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			
			if (i == 0)
				continue;

			int lastColorChar = lines[i - 1].lastIndexOf(ChatColor.COLOR_CHAR);
			if (lastColorChar == -1 || lastColorChar >= lines[i - 1].length() - 1)
				continue;

			char lastColor = lines[i - 1].charAt(lastColorChar + 1);
			lines[i] = Character.toString(ChatColor.COLOR_CHAR).concat(Character.toString(lastColor)).concat(lines[i]);	
			System.out.println(lines[i]);
		}		
		
		for (int i = 0; i < lines.length; i++)
			sender.sendMessage(lines[i]);


	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (sender.hasPermission("") && sender instanceof Player)
		{
			AnimalsCommand.execute((Player) sender);
		}
		else
		{
			Message("&cYou are not allowed to use this command!", sender);
		}
		
		return true;
	}
}
