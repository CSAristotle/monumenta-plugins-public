package pe.project.classes.Utils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class MovementUtil {
	public static void KnockAway(LivingEntity awayFromEntity, LivingEntity target, float speed) {
		Vector dir = target.getLocation().subtract(awayFromEntity.getLocation().toVector()).toVector().multiply(speed);
		dir.setY(0.5f);
		
		target.setVelocity(dir);
	}
}
