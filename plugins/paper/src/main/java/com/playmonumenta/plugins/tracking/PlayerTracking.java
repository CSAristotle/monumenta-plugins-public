package com.playmonumenta.plugins.tracking;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.playmonumenta.plugins.Constants;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.events.EvasionEvent;
import com.playmonumenta.plugins.player.PlayerData;
import com.playmonumenta.plugins.player.PlayerInventory;
import com.playmonumenta.plugins.point.Point;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.BossUtils.BossAbilityDamageEvent;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;
import com.playmonumenta.plugins.utils.ZoneUtils;
import com.playmonumenta.plugins.utils.ZoneUtils.ZoneProperty;

public class PlayerTracking implements EntityTracking {
	Plugin mPlugin = null;
	private HashMap<Player, PlayerInventory> mPlayers = new HashMap<Player, PlayerInventory>();

	PlayerTracking(Plugin plugin) {
		mPlugin = plugin;
	}

	@Override
	public void addEntity(Entity entity) {
		Player player = (Player)entity;

		// Add the tag that prevents functions and advancement rewards from being applied to the player
		player.addScoreboardTag(Constants.PLAYER_MID_TRANSFER_TAG);

		// Initialize the player, either by loading data from disk or from the player
		PlayerData.initializePlayer(mPlugin, player);

		// Remove the metadata that prevents player from interacting with things (if present)
		player.removeMetadata(Constants.PLAYER_ITEMS_LOCKED_METAKEY, mPlugin);

		// Remove the tag that prevents the spawn box from applying functions to the player
		player.removeScoreboardTag(Constants.PLAYER_MID_TRANSFER_TAG);

		// Load the players inventory / custom enchantments and apply them
		mPlayers.put(player, new PlayerInventory(mPlugin, player));
	}

	@Override
	public void removeEntity(Entity entity) {
		Player player = (Player)entity;

		// Add a scoreboard tag that prevents the spawn box from applying functions to the player
		player.addScoreboardTag(Constants.PLAYER_MID_TRANSFER_TAG);

		mPlayers.remove(player);
	}

	public Set<Player> getPlayers() {
		return mPlayers.keySet();
	}

	public void updateEquipmentProperties(Player player, Event event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.updateEquipmentProperties(mPlugin, player, event);
		}
	}

	public void onKill(Plugin plugin, Player player, Entity target, EntityDeathEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onKill(plugin, player, target, event);
		}
	}

	public void onAttack(Plugin plugin, Player player, LivingEntity target, EntityDamageByEntityEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onAttack(plugin, player, target, event);
		}
	}

	public void onDamage(Plugin plugin, Player player, LivingEntity target, EntityDamageByEntityEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onDamage(plugin, player, target, event);
		}
	}

	public void onLaunchProjectile(Plugin plugin, Player player, Projectile proj, ProjectileLaunchEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onLaunchProjectile(plugin, player, proj, event);
		}
	}

	public void onBlockBreak(Plugin plugin, Player player, BlockBreakEvent event, ItemStack item) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onBlockBreak(plugin, player, event, item);
		}
	}

	public void onPlayerInteract(Plugin plugin, Player player, PlayerInteractEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onPlayerInteract(plugin, player, event);
		}
	}

	public void onDeath(Plugin plugin, Player player, PlayerDeathEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onDeath(plugin, player, event);
		}
	}

	public void onExpChange(Plugin plugin, Player player, PlayerExpChangeEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onExpChange(plugin, player, event);
		}
	}

	public void onHurt(Plugin plugin, Player player, EntityDamageEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onHurt(plugin, player, event);
		}
	}

	public void onHurtByEntity(Plugin plugin, Player player, EntityDamageByEntityEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onHurtByEntity(plugin, player, event);
		}
	}

	public void onEvade(Plugin plugin, Player player, EvasionEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onEvade(plugin, player, event);
		}
	}

	public void onConsume(Plugin plugin, Player player, PlayerItemConsumeEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {

			manager.onConsume(plugin, player, event);
		}
	}

	public void onBossDamage(Plugin plugin, Player player, BossAbilityDamageEvent event) {
		PlayerInventory manager = mPlayers.get(player);
		if (manager != null) {
			manager.onBossDamage(plugin, player, event);
		}
	}



	@Override
	public void update(World world, int ticks) {
		Iterator<Entry<Player, PlayerInventory>> playerIter = mPlayers.entrySet().iterator();
		while (playerIter.hasNext()) {
			Entry<Player, PlayerInventory> entry = playerIter.next();
			Player player = entry.getKey();
			PlayerInventory inventory = entry.getValue();

			GameMode mode = player.getGameMode();

			if (mode != GameMode.CREATIVE && mode != GameMode.SPECTATOR) {
				Location location = player.getLocation();
				Point loc = new Point(location);

				// First we'll check if the player is too high, if so they shouldn't be here.
				if (loc.mY >= 255 && player.isOnGround()) {
					// Double check to make sure they're on the ground as it can trigger a false positive.
					Block below = world.getBlockAt(location.subtract(0, 1, 0));
					if (below != null && below.getType() == Material.AIR) {
						continue;
					}

					PlayerUtils.awardStrike(mPlugin, player, "breaking rule #5, leaving the bounds of the map.");
				} else {
					if (ZoneUtils.hasZoneProperty(player, ZoneProperty.PLOTS_POSSIBLE)) {
						boolean isInPlot = ZoneUtils.inPlot(location, mPlugin.mServerProperties.getIsTownWorld());

						if (mode == GameMode.SURVIVAL && !isInPlot) {
							player.setGameMode(GameMode.ADVENTURE);
						} else if (mode == GameMode.ADVENTURE && isInPlot
									&& loc.mY > mPlugin.mServerProperties.getPlotSurvivalMinHeight()
									&& ScoreboardUtils.getScoreboardValue(player, "Prestige") >= 3) {
							player.setGameMode(GameMode.SURVIVAL);
						}
					}
				}

				// Give potion effects to those in a City;
				if (ZoneUtils.hasZoneProperty(player, ZoneProperty.SPEED_2)) {
					mPlugin.mPotionManager.addPotion(player, PotionID.SAFE_ZONE, Constants.CAPITAL_SPEED_EFFECT);
				}
				if (ZoneUtils.hasZoneProperty(player, ZoneProperty.MASK_SPEED)) {
					mPlugin.mPotionManager.addPotion(player, PotionID.SAFE_ZONE, Constants.CITY_SPEED_MASK_EFFECT);
				}
				if (ZoneUtils.hasZoneProperty(player, ZoneProperty.RESIST_5)) {
					mPlugin.mPotionManager.addPotion(player, PotionID.SAFE_ZONE, Constants.CITY_RESISTANCE_EFFECT);
				}
				if (ZoneUtils.hasZoneProperty(player, ZoneProperty.SATURATION_2)) {
					mPlugin.mPotionManager.addPotion(player, PotionID.SAFE_ZONE, Constants.CITY_SATURATION_EFFECT);
				}
				if (ZoneUtils.hasZoneProperty(player, ZoneProperty.MASK_JUMP_BOOST)) {
					mPlugin.mPotionManager.addPotion(player, PotionID.SAFE_ZONE, Constants.CITY_JUMP_MASK_EFFECT);
				}
			}

			// Extra Effects
			try {
				inventory.tick(mPlugin, world, player);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				mPlugin.mPotionManager.updatePotionStatus(player, ticks);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void unloadTrackedEntities() {
		Iterator<Entry<Player, PlayerInventory>> iter = mPlayers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Player, PlayerInventory> entry = iter.next();
			Player player = entry.getKey();
			entry.getValue().removeProperties(mPlugin, player);
		}

		mPlayers.clear();
	}
}
