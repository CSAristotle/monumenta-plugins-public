package com.playmonumenta.bossfights.bosses.gray;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.bossfights.SpellManager;
import com.playmonumenta.bossfights.bosses.BossAbilityGroup;
import com.playmonumenta.bossfights.spells.SpellBaseSummon;
import com.playmonumenta.bossfights.utils.Utils;

public abstract class GrayStrongSummonerBase extends BossAbilityGroup {
	private static final int SUMMON_TIME = 300;
	private static final int TIME_BETWEEN_CASTS = 800;
	private static final int SUMMON_TICK_PERIOD = 1;
	private static final int PLAYER_RADIUS = 7;
	private static final int SPAWNS_PER_PLAYER = 3;

	GrayStrongSummonerBase(Plugin plugin, LivingEntity boss, String identityTag, int detectionRange, String mobType, String mobNBT) throws Exception {
		if (!(boss instanceof Mob)) {
			throw new Exception("gray boss tags only work on mobs!");
		}

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellBaseSummon(plugin, SUMMON_TIME, TIME_BETWEEN_CASTS, PLAYER_RADIUS, SPAWNS_PER_PLAYER, false,
				() -> {
					// Run on all nearby players
					//TODO: Logarithmic instead?
					return Utils.playersInRange(boss.getLocation(), 20);
				},
				(summonLoc, player) -> {
					try {
						Location loc = summonLoc.clone().subtract(0, 2.5f, 0);
						Entity entity = Utils.summonEntityAt(loc, mobType, mobNBT);
						if (entity != null && entity instanceof Mob) {
							Mob mob = (Mob)entity;
							mob.setAI(false);

							BukkitRunnable runnable = new BukkitRunnable() {
								int mTicks = 0;

								@Override
								public void run() {
									mTicks++;

									if (mTicks < SUMMON_TIME) {
										Location mobLoc = mob.getLocation().add(0, 1.8f/(SUMMON_TIME / SUMMON_TICK_PERIOD), 0);
										mob.teleport(mobLoc);

										//TODO: Helix
										summonLoc.getWorld().spawnParticle(Particle.SPELL_INSTANT, summonLoc, 2, 0.5, 0.5, 0.5, 0);
									} else {
										mob.setAI(true);
										this.cancel();
									}
								}

								// If this is cancelled before the mob has finished summoning, kill the mob
								@Override
								public void cancel() {
									super.cancel();
									if (mTicks < SUMMON_TIME) {
										//TODO: Better sound/particle
										summonLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, summonLoc, 2, 0.3, 0.3, 0.3, 0);
										summonLoc.getWorld().playSound(summonLoc, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1f, 0.5f);
										mob.damage(1000);
									}
								}
							};

							runnable.runTaskTimer(plugin, 1, SUMMON_TICK_PERIOD);

							return runnable;
						} else {
							plugin.getLogger().warning("Summoned mob but got something other than mob back!");
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					return null;
				},
				() -> {
					//TODO: Sound
					BukkitRunnable runnable = new BukkitRunnable() {
						int mTicks = 0;

						@Override
						public void run() {
							mTicks++;

							if (mTicks < SUMMON_TIME) {
								((Mob)boss).setTarget(null);

								//TODO: Helix
								Location mobLoc = boss.getLocation();
								mobLoc.getWorld().spawnParticle(Particle.SPELL_INSTANT, mobLoc, 2, 0.5, 0.5, 0.5, 0);
							} else {
								this.cancel();
							}
						}
					};

					runnable.runTaskTimer(plugin, 1, 1);

					return runnable;
				}
			)
		));

		super.constructBoss(plugin, identityTag, boss, activeSpells, null, detectionRange, null);
	}
}