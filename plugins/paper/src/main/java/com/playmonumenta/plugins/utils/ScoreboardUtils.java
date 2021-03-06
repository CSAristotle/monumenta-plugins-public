package com.playmonumenta.plugins.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.plugins.Plugin;

public class ScoreboardUtils {
	public static final String[] NOT_TRANSFERRED_OBJECTIVES_VALS =
	    new String[] {"Apartment", "AptIdle", "VotesWeekly", "VotesTotal", "VotesSinceWin", "VoteRewards", "VoteRaffle", "VoteCache", "KaulSpleefWins", "SnowmanKills"};
	public static final Set<String> NOT_TRANSFERRED_OBJECTIVES =
	    new HashSet<>(Arrays.asList(NOT_TRANSFERRED_OBJECTIVES_VALS));

	/**
	 * Get scoreboard value for player.
	 *
	 * @param player          the player object
	 * @param scoreboardValue the objective name
	 * @return the objective value associated with the player
	 */
	public static int getScoreboardValue(Player player, String scoreboardValue) {
		Objective objective = player.getScoreboard().getObjective(scoreboardValue);

		if (objective != null) {
			return objective.getScore(player.getName()).getScore();
		}

		return 0;
	}

	/**
	 * Get scoreboard value for player.
	 *
	 * @param playerName      the player name
	 * @param scoreboardValue the object name
	 * @return the objective value associated with the player
	 */
	public static Optional<Integer> getScoreboardValue(String playerName, String scoreboardValue) {
		Optional<Integer> scoreValue = Optional.empty();
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(scoreboardValue);

		if (objective != null) {
			Score score = objective.getScore(playerName);
			if (score != null) {
				scoreValue = Optional.of(score.getScore());
			}
		}

		return scoreValue;
	}

	public static void setScoreboardValue(String playerName, String scoreboardValue, int value) {
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(scoreboardValue);
		if (objective != null) {
			Score score = objective.getScore(playerName);
			score.setScore(value);
		}
	}

	public static void setScoreboardValue(Player player, String scoreboardValue, int value) {
		Objective objective = player.getScoreboard().getObjective(scoreboardValue);
		if (objective != null) {
			Score score = objective.getScore(player.getName());
			score.setScore(value);
		}
	}

	public static JsonObject getAsJsonObject(Player player) {
		// returned data contains an array of scoreboard key/value pairs and an array of tags
		JsonObject returnData = new JsonObject();

		// Scoreboards
		JsonArray scoreboardArray = new JsonArray();

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Set<Objective> objectives = scoreboard.getObjectives();

		for (Objective objective : objectives) {
			Score score = objective.getScore(player.getName());
			if (score != null) {
				JsonObject scoreboardInfo = new JsonObject();

				scoreboardInfo.addProperty("name", objective.getName());
				scoreboardInfo.addProperty("score", score.getScore());

				scoreboardArray.add(scoreboardInfo);
			}
		}
		returnData.add("scores", scoreboardArray);

		// Tags
		JsonArray tagArray = new JsonArray();
		for (String tag : player.getScoreboardTags()) {
			tagArray.add(tag);
		}
		returnData.add("tags", tagArray);


		/*
		 * "team" : [
		 *  "name" : "theName",
		 *  "displayName" : "theDisplayName",
		 *  "prefix" : "thePrefix",
		 *  "suffix" : "theSuffix",
		 *  "color" : "theColor",
		 *  "members" : [
		 *   "member1",
		 *   "member2",
		 *  ]
		 * ]
		 */
		Team team = scoreboard.getEntryTeam(player.getName());
		if (team != null) {
			JsonObject teamObject = new JsonObject();

			String name = team.getName();
			if (name == null) {
				name = "";
			}
			teamObject.addProperty("name", name);

			String displayName = team.getDisplayName();
			if (displayName == null) {
				displayName = "";
			}
			teamObject.addProperty("displayName", displayName);

			String prefix = team.getPrefix();
			if (prefix == null) {
				prefix = "";
			}
			teamObject.addProperty("prefix", prefix);

			String suffix = team.getSuffix();
			if (suffix == null) {
				suffix = "";
			}
			teamObject.addProperty("suffix", suffix);

			ChatColor color = team.getColor();
			if (color == null) {
				color = ChatColor.WHITE;
			}
			teamObject.addProperty("color", color.name());

			JsonArray teamMembers = new JsonArray();
			for (String entry : team.getEntries()) {
				teamMembers.add(entry);
			}
			teamObject.add("members", teamMembers);

			// Add this whole collection to the player data
			returnData.add("team", teamObject);
		}

		return returnData;
	}

	public static void loadFromJsonObject(Player player, JsonObject object) throws Exception {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		// Load scoreboards first
		Iterator<JsonElement> scoreIter = object.get("scores").getAsJsonArray().iterator();
		while (scoreIter.hasNext()) {
			JsonObject scoreboardObject = scoreIter.next().getAsJsonObject();

			String name = scoreboardObject.get("name").getAsString();
			if (NOT_TRANSFERRED_OBJECTIVES.contains(name)) {
				/* This objective is not transferred/loaded */
				continue;
			}
			int scoreVal = scoreboardObject.get("score").getAsInt();

			Objective objective = scoreboard.getObjective(name);
			if (objective == null) {
				objective = scoreboard.registerNewObjective(name, "dummy", name);
			}

			Score score = objective.getScore(player.getName());
			score.setScore(scoreVal);
		}

		// Remove player's tags
		player.getScoreboardTags().clear();

		// Add player tags from JSON
		Iterator<JsonElement> tagIter = object.get("tags").getAsJsonArray().iterator();
		while (tagIter.hasNext()) {
			player.addScoreboardTag(tagIter.next().getAsString());
		}

		Team currentTeam = scoreboard.getEntryTeam(player.getName());

		if (object.has("team")) {
			JsonObject teamObject = object.get("team").getAsJsonObject();

			String name = teamObject.get("name").getAsString();
			String displayName = teamObject.get("displayName").getAsString();
			String prefix = teamObject.get("prefix").getAsString();
			String suffix = teamObject.get("suffix").getAsString();
			String color = teamObject.get("color").getAsString();

			Team newTeam = null;
			if (currentTeam != null) {
				if (currentTeam.getName().equals(name)) {
					// Already on this team
					newTeam = currentTeam;
				} else {
					// Joined to a different team - need to leave it
					currentTeam.removeEntry(player.getName());

					// If the team is empty, remove it
					if (currentTeam.getSize() <= 0) {
						currentTeam.unregister();
					}
				}
			}

			// If newTeam still null, need to join to it
			if (newTeam == null) {
				// Look up the right team
				newTeam = scoreboard.getTeam(name);

				// If newTeam *still* null, this team doesn't exist
				if (newTeam == null) {
					newTeam = scoreboard.registerNewTeam(name);
				}

				// Join player to the team
				newTeam.addEntry(player.getName());
			}

			newTeam.setDisplayName(displayName);
			newTeam.setPrefix(prefix);
			newTeam.setSuffix(suffix);
			newTeam.setColor(ChatColor.valueOf(color));

			// Note - team member list not used here
		}
	}

	public static void transferPlayerScores(Plugin plugin, String from, Player to) throws Exception {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Set<Objective> objectives = scoreboard.getObjectives();

		boolean fromPlayerExist = scoreboard.getEntries().contains(from);
		boolean toPlayerExist = scoreboard.getEntries().contains(to.getName());

		if (!fromPlayerExist) {
			throw new Exception("Old player scoreboard does not exist. Have they ever been on the server or was the name typed incorrectly?");
		}

		if (!toPlayerExist) {
			throw new Exception("New player scoreboard does not exist. Have they ever been on the server or was the name typed incorrectly?");
		}

		// Additionally to prevent any potential fuck ups by people using this....we want to make sure the from player is offline
		// and the 'to' player is online...

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().equals(from) || !to.isOnline()) {
				throw new Exception("Can only transfer scores from an offline player to an online player. (To prevent accidently breaking people)");
			}
		}

		// Transfer Scoreboards from the old name to the new name!
		for (Objective objective : objectives) {
			Score toScore = objective.getScore(to.getName());
			Score fromScore = objective.getScore(from);
			if (toScore != null && fromScore != null) {
				toScore.setScore(fromScore.getScore());
			}
		}

		// Reset the 'from' player's scores if everything went well up to this point
		NetworkUtils.broadcastCommand(plugin, "scoreboard players reset " + from);
	}
}
