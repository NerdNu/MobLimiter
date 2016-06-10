package nu.nerd.moblimiter;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Mob-related methods that may be shared across classes
 */
public class EntityHelper {


    /**
     * Check if this is a mob that should ever be limited.
     * Merely checking Animals or Monster subclassing is not sufficient due to Bukkit inconsistency.
     * e.g. Armor Stands are LivingEntities, apparently.
     * So to check whether something is a limitable mob, we check if it's a LivingEntity first and then
     * apply a blacklist of edge cases such as Armor Stands.
     * @param entity The entity to check
     * @return true if the entity is a limitable mob
     */
    public static boolean isLimitableMob(Entity entity) {
        List<EntityType> blacklist = new ArrayList<EntityType>();
        blacklist.add(EntityType.ARMOR_STAND);
        blacklist.add(EntityType.PLAYER);
        return entity instanceof LivingEntity && !blacklist.contains(entity.getType());
    }


    /**
     * Check if this is a "special mob" that shouldn't be removed in any circumstance
     * @param entity entity to check
     * @return true if this is a special mob
     */
    public static boolean isSpecialMob(LivingEntity entity) {

        // Keep mobs with custom names
        if (entity.getCustomName() != null) {
            return true;
        }

        // Don't remove tamed mobs
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.isTamed()) {
                return true;
            }
        }

        // Save the sponge!
        if (entity.getType() == EntityType.GUARDIAN && ((Guardian)entity).isElder()) {
            return true;
        }

        // Don't remove mobs that are holding something, which they may have picked up
        EntityEquipment equipment = entity.getEquipment();
        for (ItemStack armor : equipment.getArmorContents()) {
            // Unarmored mobs, even animals, spawn with 1xAIR as armor.
            if (armor != null && armor.getType() != Material.AIR) {
                return true;
            }
        }

        return false;

    }


    /**
     * If this is an Animal and there are two or less in the chunk, don't remove them.
     * This protects breeding pairs for farm animals.
     * @param entity the entity to check
     * @return false if the entity doesn't match the criteria
     */
    public static boolean isBreedingPair(Entity entity) {
        if (!(entity instanceof Animals)) return false;
        if (entity instanceof Tameable) return false;
        int count = 0;
        for (Entity e : entity.getLocation().getChunk().getEntities()) {
            if (e.getType().equals(entity.getType()) && !e.isDead()) {
                count++;
            }
        }
        return count < 3;
    }


    /**
     * Return details about an entity for debugging
     * @param entity the entity
     * @return String representation of the mob name and location
     */
    public static String getMobDescription(Entity entity) {
        String type = entity.getType().toString();
        String world = entity.getLocation().getWorld().getName();
        Location loc = entity.getLocation();
        return String.format("%s at (%s,%d,%d,%d)", type, world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }


    /**
     * Return the mob a player is looking at
     * @param player The player to check
     * @return null or LivingEntity
     */
    public static LivingEntity getMobInLineOfSight(Player player) {
        List<Entity> entities = player.getNearbyEntities(5, 1, 5);
        Iterator<Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            Entity ent = iterator.next();
            if (!(ent instanceof LivingEntity)) iterator.remove();
        }
        for (Block block : player.getLineOfSight((Set) null, 6)) {
            if (block.getType() != Material.AIR) break; //view is obstructed
            for (Entity ent : entities) {
                Vector b = block.getLocation().toVector();
                Vector head = ent.getLocation().toVector().add(new Vector(0, 1, 0));
                Vector foot = ent.getLocation().toVector();
                if (head.isInSphere(b, 1.25) || foot.isInSphere(b, 1.25)) {
                    return (LivingEntity) ent;
                }
            }
        }
        return null;
    }


    /**
     * Build a HashMap of entity types and their counts in a chunk
     * @param chunk The chunk to check
     * @return A HashMap summarizing the entity breakdown of a chunk
     */
    public static HashMap<String, Integer> summarizeMobsInChunk(Chunk chunk) {
        HashMap<String, Integer> chunkCounts = new HashMap<String, Integer>();
        for (Entity e : chunk.getEntities()) {
            if (e.isDead() || !isLimitableMob(e)) continue;
            if (chunkCounts.containsKey(e.getType().toString())) {
                int count = chunkCounts.get(e.getType().toString()) + 1;
                chunkCounts.put(e.getType().toString(), count);
            } else {
                chunkCounts.put(e.getType().toString(), 1);
            }
        }
        return chunkCounts;
    }


    /**
     * Build a HashMap of entity types and their counts in a chunk radius
     * @param start The center of the chunk radius
     * @param radius The radius of chunks to check
     * @return A HashMap summarizing the entity breakdown within a chunk radius
     */
    public static HashMap<String, Integer> summarizeMobsInRadius(Chunk start, int radius) {
        HashMap<String, Integer> radCounts = new HashMap<String, Integer>();
        World world = start.getWorld();
        for (int x = start.getX() - radius; x <= start.getX() + radius; x++) {
            for (int z = start.getZ() - radius; z <= start.getZ() + radius; z++) {
                Chunk c = world.getChunkAt(x, z);
                for (Entity e : c.getEntities()) {
                    if (e.isDead() || !isLimitableMob(e)) continue;
                    if (radCounts.containsKey(e.getType().toString())) {
                        int count = radCounts.get(e.getType().toString()) + 1;
                        radCounts.put(e.getType().toString(), count);
                    } else {
                        radCounts.put(e.getType().toString(), 1);
                    }
                }
            }
        }
        return radCounts;
    }


}
