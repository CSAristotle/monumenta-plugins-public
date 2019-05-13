package com.playmonumenta.bossfights.bosses;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.bossfights.SpellManager;
import com.playmonumenta.bossfights.spells.SpellBaseLaser;
import com.playmonumenta.bossfights.utils.DamageUtils;

public class PulseLaserBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_pulselaser";
	public static final int detectionRange = 30;

	LivingEntity mBoss;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new PulseLaserBoss(plugin, boss);
	}

	public PulseLaserBoss(Plugin plugin, LivingEntity boss) {
		mBoss = boss;

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false, 240,
		                       // Tick action per player
		                       (Player player, int ticks, boolean blocked) -> {
		                           player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 0.8f, 0.5f + ((float)ticks / 80f) * 1.5f);
		                           boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 0.8f, 0.5f + ((float)ticks / 80f) * 1.5f);
		                           if (ticks == 0) {
		                               boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4), true);
		                           }
		                       },
		                       // Particles generated by the laser
		                       (Location loc) -> {
		                           loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.02, 0.02, 0.02, 0);
		                           loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 1, 0.04, 0.04, 0.04, 1);
		                       },
		                       // Damage generated at the end of the attack
		                       (Player player, Location loc, boolean blocked) -> {
		                           loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.6f, 1.5f);
		                           loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 35, 0, 0, 0, 0.25);
		                           if (!blocked) {
		                               DamageUtils.damage(null, player, 12);
		                           }
		                       })
		));

		super.constructBoss(plugin, identityTag, mBoss, activeSpells, null, detectionRange, null);
	}
}
