package nu.nerd.moblimiter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
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
	
	public int numberControlRadiusSquared;
	public int maxOneType;
	public int maxAnyType;
	
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
		
		breedLimitOneAnimalMessage = this.getConfig().getString("Messages.BreedLimitOneAnimal", "You cannot breed this <Animal> because there is more than <AnimalTypeLimit> <AnimalPlural> in <MaxAnimalRadius> block radius around you.");
		breedLimitAllAnimalsMessage = this.getConfig().getString("Messages.BreedLimitAllAnimals", "You cannot breed this <Animal> because you cannot have more than <AnimalsLimit> animals in <MaxAnimalRadius> blocks radius around you.");

		
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
			String message = null;
			if (breedPossibility == -1)
			{
				message = breedLimitAllAnimalsMessage;
				message = message.replace("<AnimalsLimit>", Integer.toString(maxAnyType));
			}
			else
			{
				message = breedLimitOneAnimalMessage;
				message = message.replace("<AnimalPlural>", animalPlurals.get(ent.getType()));
			}
			
			message = message.replace("<Animal>", animalSingulars.get(ent.getType()));
			message = message.replace("<MaxAnimalRadius>", Integer.toString((int) Math.round(Math.sqrt(numberControlRadiusSquared))));
		
			Message(message, player);
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
			return food == Material.SEEDS || food == Material.PUMPKIN_SEEDS || food == Material.NETHER_STALK;
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
				if (++numberOfAllTypes > maxAnyType)
					return -1;
				
				if (type == animal.getType() && ++numberOfOneType > maxOneType)
					return -2;
			}
		}
		
		return 0;
	}
	
	public static void Message(String message, CommandSender sender)
	{
		message = message.replaceAll("\\&([0-9abcdef])", ChatColor.COLOR_CHAR + "$1");

		String color = "f";
		final int maxLength = 59; //Max length of chat text message
		final String newLine = "[NEWLINE]";
		ArrayList<String> chat = new ArrayList<String>();
		chat.add(0, "");
		String[] words = message.split(" ");
		int lineNumber = 0;
		for (int i = 0; i < words.length; i++) {
			if (chat.get(lineNumber).replaceAll("\\" + ChatColor.COLOR_CHAR + "([0-9abcdef])", "").length() + words[i].replaceAll("\\" + ChatColor.COLOR_CHAR + "([0-9abcdef])", "").length() < maxLength && !words[i].equals(newLine)) {
				chat.set(lineNumber, chat.get(lineNumber) + (chat.get(lineNumber).length() > 0 ? " " : ChatColor.COLOR_CHAR + color ) + words[i]);

				if (words[i].indexOf(ChatColor.COLOR_CHAR) != -1) color = Character.toString(words[i].charAt(words[i].lastIndexOf(ChatColor.COLOR_CHAR) + 1));
			}
			else {
				lineNumber++;
				if (!words[i].equals(newLine)) {
					chat.add(lineNumber, ChatColor.COLOR_CHAR + color + words[i]);
				}
				else
					chat.add(lineNumber, "");
			}
		}
		for (int i = 0; i < chat.size(); i++) {
			{
				sender.sendMessage(chat.get(i));
			}

		}
	}
}
