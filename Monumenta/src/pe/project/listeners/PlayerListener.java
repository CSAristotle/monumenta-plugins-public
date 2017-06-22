package pe.project.listeners;

import java.util.Iterator;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import pe.project.Main;
import pe.project.locations.poi.PointOfInterest;
import pe.project.point.Point;
import pe.project.server.reset.RegionReset;
import pe.project.utils.ItemUtils;
import pe.project.utils.ScoreboardUtils;

public class PlayerListener implements Listener {
	Main mPlugin = null;
	
	public PlayerListener(Main plugin) {
		mPlugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		mPlugin.mTrackingManager.addEntity(player);
		RegionReset.handle(mPlugin, player);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerQuitEvent(PlayerQuitEvent event) {
		mPlugin.mTrackingManager.removeEntity(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerInteractEvent(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		
		//	Left Click.
		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			//	Quest Compass.
			if (item != null && item.getType() == Material.COMPASS) {
				//	Show currently active quest.
				mPlugin.mQuestManager.showCurrentQuest(player);
			}
		}
		//	Right Click.
		else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			//	Quest Compass.
			if (item != null && item.getType() == Material.COMPASS) {
				//	Show current POI respawn timer.
				if (player.isSneaking()) {
					List<PointOfInterest> pois = mPlugin.mPOIManager.allWithinAnyPointOfInterest(new Point(player.getLocation()));
					if (pois != null && pois.size() > 0) {
						for (PointOfInterest poi : pois) {
							int ticks = poi.getTimer();
	
							int minutes = (int)((ticks / 20) / 60);
							int seconds = ((ticks - ((minutes * 60) * 20))) / 20;
							
							String time = "";
							if (minutes > 0) {
								time = minutes + " minutes ";
							}
							
							time += seconds + " seconds";
							
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD +  poi.getName() + " is respawning in " + time);
						}
					} else {
						player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are not within range of a Point of Interest.");
					}
				}
				//	Cycle active Quest.
				else {
					mPlugin.mQuestManager.cycleQuestTracker(event.getPlayer());
				}
			}
			
			if (player.getGameMode() == GameMode.ADVENTURE) {
				ItemStack heldItem = player.getInventory().getItemInMainHand();
				if (ItemUtils.isBoat(heldItem.getType())) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity instanceof ZombieVillager) {
			Player player = event.getPlayer();
			ItemStack item = player.getEquipment().getItemInMainHand();
			if (item != null) {
				Material type = item.getType();
				if (type == Material.GOLDEN_APPLE) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		Location playerLoc = player.getLocation();
		int guild = ScoreboardUtils.getScoreboardValue(player, "Guild");
		
		Iterator<Player> iter = event.getRecipients().iterator();
		while (iter.hasNext()) {
			Player receiver = iter.next();
			int receiverGuild = ScoreboardUtils.getScoreboardValue(receiver, "Guild");
			
			if (guild == 0 || guild != receiverGuild) {
				int chatDistance = ScoreboardUtils.getScoreboardValue(receiver, "chatDistance");			
				if (playerLoc.distance(receiver.getLocation()) > chatDistance) {
					iter.remove();
				}
			}
		}
	}
}
