package com.playmonumenta.plugins.abilities.other;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.events.EvasionEvent;
import com.playmonumenta.plugins.utils.BossUtils.BossAbilityDamageEvent;

public class EvasionEnchant extends Ability {
	public double mChance = 0;
	private int mLastActivationTick = 0;

	public EvasionEnchant(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player, null);
	}

	@Override
	public boolean playerDamagedByLivingEntityEvent(EntityDamageByEntityEvent event) {
		event.setDamage(evade(mPlayer, event.getDamage(), event.getFinalDamage()));
		return true;
	}

	@Override
	public boolean playerDamagedByProjectileEvent(EntityDamageByEntityEvent event) {
		event.setDamage(evade(mPlayer, event.getDamage(), event.getFinalDamage()));
		return true;
	}

	@Override
	public void playerDamagedByBossEvent(BossAbilityDamageEvent event) {
		// We don't have a good way to calculate final damage of this yet
		event.setDamage(evade(mPlayer, event.getDamage(), event.getDamage()));
	}

	private double evade(Player player, double damage, double finalDamage) {
		double changedDamage = damage;
		Location loc = player.getLocation().add(0, 1, 0);
		if (mChance >= 200 || mChance >= 100 && mPlayer.getHealth() <= finalDamage / 2) {
			mChance -= 200;
			changedDamage *= 0.25;
			if (Math.abs(player.getTicksLived() - mLastActivationTick) < 160) {
				mWorld.spawnParticle(Particle.SMOKE_NORMAL, loc, 16, 0.15, 0.25, 0.15, 0.05);
				mWorld.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.2f, 1.5f);
				mWorld.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2f);
			} else {
				mWorld.spawnParticle(Particle.SMOKE_NORMAL, loc, 80, 0.25, 0.45, 0.25, 0.1);
				mWorld.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1.5f);
				mWorld.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1, 2f);
			}
			EvasionEvent event = new EvasionEvent(player);
			Bukkit.getPluginManager().callEvent(event);
		} else if (mChance >= 100 || mChance >= 50 && mPlayer.getHealth() <= finalDamage) {
			mChance -= 100;
			changedDamage *= 0.5;
			if (Math.abs(player.getTicksLived() - mLastActivationTick) < 160) {
				mWorld.spawnParticle(Particle.SMOKE_NORMAL, loc, 16, 0.15, 0.25, 0.15, 0.05);
				mWorld.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.2f, 1.5f);
			} else {
				mWorld.spawnParticle(Particle.SMOKE_NORMAL, loc, 80, 0.25, 0.45, 0.25, 0.1);
				mWorld.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1.5f);
			}
			EvasionEvent event = new EvasionEvent(player);
			Bukkit.getPluginManager().callEvent(event);
		}
		mLastActivationTick = player.getTicksLived();
		return changedDamage;
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}

}
