package com.playmonumenta.plugins.abilities.other;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;

public class PatreonPurple extends Ability {
	private boolean mNoSelfParticles = false;

	public PatreonPurple(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player, null);

		if (player != null) {
			mNoSelfParticles = player.getScoreboardTags().contains("noSelfParticles");
		} else {
			mNoSelfParticles = false;
		}
	}

	@Override
	public boolean canUse(Player player) {
		int patreon = ScoreboardUtils.getScoreboardValue(player, "Patreon");
		int shinyPurple = ScoreboardUtils.getScoreboardValue(player, "ShinyPurple");
		return shinyPurple > 0 && patreon >= 10;
	}

	@Override
	public void periodicTrigger(boolean fourHertz, boolean twoHertz, boolean oneSecond, int ticks) {
		if (fourHertz) {
			if (mNoSelfParticles) {
				for (Player other : PlayerUtils.playersInRange(mPlayer, 30, false)) {
					other.spawnParticle(Particle.DRAGON_BREATH, mPlayer.getLocation().add(0, 0.2, 0), 4, 0.25, 0.25, 0.25, 0);
				}
			} else {
				mWorld.spawnParticle(Particle.DRAGON_BREATH, mPlayer.getLocation().add(0, 0.2, 0), 4, 0.25, 0.25, 0.25, 0);
			}
		}
	}
}
