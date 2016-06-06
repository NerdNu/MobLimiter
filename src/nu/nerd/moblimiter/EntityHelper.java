package nu.nerd.moblimiter;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Mob-related methods that may be shared across classes
 */
public class EntityHelper {


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
            if (e.getType().equals(entity.getType())) {
                count++;
            }
        }
        return count < 3;
    }


    public static String getMobDescription(Entity entity) {
        String type = entity.getType().toString();
        String world = entity.getLocation().getWorld().getName();
        Location loc = entity.getLocation();
        return String.format("%s at (%s,%d,%d,%d)", type, world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }


}
