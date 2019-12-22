package com.playmonumenta.plugins.bosses.spells;

import java.util.Collections;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import com.playmonumenta.plugins.bosses.utils.Utils;

/*
 * This spell is designed to run as a passive. Here are the rules:
 * - Do not change mob target more than once every 3s
 * - If mob has line of sight to player within range, set cooldown to change target back to 3s
 * - If mob does not have line of sight to player, tick down cooldown
 * - If cooldown elapsed, mob targets the closest player with line of sight
 * - If no players have line of sight, the mob continues to target the last seen player, even through walls
 */
public class SpellTargetVisiblePlayer extends Spell {
	private final int PERIOD = 5;

	private final Mob mBoss;
	private final int mDetectionRange;
	private final int mCooldown;
	private final int mForgetTicks;

	private int mCooldownRemaining;
	private int mTicksSinceLastSeen;
	private Player mLastTarget;

	public SpellTargetVisiblePlayer(Mob launcher, int detectionRange, int targetSwitchCooldown, int forgetTicks) {
		mBoss = launcher;
		mDetectionRange = detectionRange;
		mCooldown = targetSwitchCooldown;
		mForgetTicks = forgetTicks;

		mCooldownRemaining = 0;
		mLastTarget = null;
	}

	@Override
	public void cancel() {
		mBoss.setTarget(null);
	}

	@Override
	public void run() {
		mCooldownRemaining -= PERIOD;
		mTicksSinceLastSeen += PERIOD;

		// Forget about this target if they leave the game or switch to spectator or are invisible
		if (mLastTarget != null) {
			if (mBoss.hasMetadata("MobIsStunnedByEntityUtils") || mBoss.hasMetadata("MobIsConfusedByEntityUtils") || !mLastTarget.isOnline() ||
					mLastTarget.getGameMode().equals(GameMode.SPECTATOR) || mLastTarget.hasMetadata("CloakAndDaggerPlayerIsInvisible")) {
				mLastTarget = null;
				mBoss.setTarget(null);
				mCooldownRemaining = 0;
			}
		}

		// Check if current target is still visible
		if (mLastTarget != null && Utils.hasLineOfSight(mBoss.getEyeLocation(), mLastTarget)) {
			mTicksSinceLastSeen = 0;

			// Make sure that player is still the target
			if (mBoss.getTarget() != mLastTarget) {
				mBoss.setTarget(mLastTarget);
			}

			// Refresh the cooldown and end here
			mCooldownRemaining = mCooldown;
		} else {
			if (mCooldownRemaining > 0) {
				// Continue targeting last target even though they are not visible
				if (mLastTarget != null && mBoss.getTarget() != mLastTarget) {
					mBoss.setTarget(mLastTarget);
				}
			} else if (!mBoss.hasMetadata("MobIsStunnedByEntityUtils")) {
				// Potentially find a new target if not stunned
				Location bossLoc = mBoss.getEyeLocation();
				List<Player> potentialTargets = Utils.playersInRange(bossLoc, mDetectionRange);
				Collections.sort(potentialTargets, (a, b) -> Double.compare(a.getLocation().distance(bossLoc), b.getLocation().distance(bossLoc)));

				for (Player player : potentialTargets) {
					if (Utils.hasLineOfSight(mBoss.getEyeLocation(), player) && !player.hasMetadata("CloakAndDaggerPlayerIsInvisible")) {
						mLastTarget = player;
						mBoss.setTarget(player);
						mCooldownRemaining = mCooldown;
						mTicksSinceLastSeen = 0;
						break;
					}
				}

				if (mTicksSinceLastSeen > mForgetTicks) {
					// Haven't seen any players in a while - resume wandering
					mBoss.setTarget(null);
					mLastTarget = null;
					mCooldownRemaining = 0;
				}
			}
		}
	}

	@Override
	public int duration() {
		return PERIOD;
	}
}