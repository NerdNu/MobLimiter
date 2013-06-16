package nu.nerd.moblimiter;

import java.util.Collection;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

public class AnimalsCommand {
	public static void execute(Player player)
	{
		int total = 0;
		int cows = 0;
		int sheep = 0;
		int pigs = 0;
		int chickens = 0;
		boolean over = false;
		
		Collection<Animals> animals = player.getWorld().getEntitiesByClass(Animals.class);
		
		for (Animals animal : animals)
		{
			int distance = (int) animal.getLocation().distanceSquared(player.getLocation());
			if (distance > MobLimiter.numberControlRadiusSquared)
				continue;
			
			if (animal instanceof Cow)
			{
				cows++;
				total++;
				
				if (cows > MobLimiter.maxOneType || total > MobLimiter.maxAnyType)
					over = true;
			}
			else if (animal instanceof Sheep)
			{
				sheep++;
				total++;
				
				if (sheep > MobLimiter.maxOneType || total > MobLimiter.maxAnyType)
					over = true;
			}
			else if (animal instanceof Pig)
			{
				pigs++;
				total++;
				
				if (pigs > MobLimiter.maxOneType || total > MobLimiter.maxAnyType)
					over = true;
			}
			else if (animal instanceof Chicken)
			{				
				chickens++;
				total++;
				
				if (chickens > MobLimiter.maxOneType || total > MobLimiter.maxAnyType)
					over = true;
			}
		}
		
		String message = "Total: &" +  getColor(total, true) + total + "/" + MobLimiter.maxAnyType + "&f in loaded area around you [NEWLINE]";
		message += "Cows: &" + getColor(cows, false) + cows + "/" + MobLimiter.maxOneType + "&f, ";
		message += "Sheep: &" + getColor(sheep, false) + sheep + "/" + MobLimiter.maxOneType + "&f [NEWLINE]";
		message += "Pigs: &" + getColor(pigs, false) + pigs + "/" + MobLimiter.maxOneType + "&f, ";
		message += "Chickens: &" + getColor(chickens, false) + chickens + "/" + MobLimiter.maxOneType;
		
		if (over)
		{
			message += " [NEWLINE] " + "&cYou have too many animals for the server to handle. Please, if you can, consider killing some or moving them further to keep the server healthy. Many thanks, we appreciate it.";
		}
		
		MobLimiter.Message(message, player);
	}
	
	private static String getColor(int number, boolean allAnimals)
	{
		int max = allAnimals ? MobLimiter.maxAnyType : MobLimiter.maxOneType;
		
		if (number < max * 0.5)
			return "2";
		else if (number < max * 0.7)
			return "e";
		else if (number < max)
			return "6";
		else 
			return "c";
	}
}
