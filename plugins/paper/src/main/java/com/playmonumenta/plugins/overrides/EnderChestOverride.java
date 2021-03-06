package com.playmonumenta.plugins.overrides;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.ZoneUtils;
import com.playmonumenta.plugins.utils.ZoneUtils.ZoneProperty;

public class EnderChestOverride extends BaseOverride {
	@Override
	public boolean blockPlaceInteraction(Plugin plugin, Player player, ItemStack item, BlockPlaceEvent event) {
		if (player == null || player.getGameMode() == GameMode.CREATIVE) {
			return true;
		} else if (player.getGameMode() == GameMode.SURVIVAL && ZoneUtils.hasZoneProperty(player, ZoneProperty.PLOTS_POSSIBLE)) {
			return true;
		}

		return false;
	}

	/* Chests placed on barriers can not be broken */
	@Override
	public boolean blockBreakInteraction(Plugin plugin, Player player, Block block) {
		if ((player.getGameMode() == GameMode.CREATIVE) || ChestOverride.breakable(block)) {
			return true;
		}
		return false;
	}
}
