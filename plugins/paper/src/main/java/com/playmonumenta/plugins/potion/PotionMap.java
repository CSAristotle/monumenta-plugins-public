package com.playmonumenta.plugins.potion;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.PotionUtils;
import com.playmonumenta.plugins.utils.PotionUtils.PotionInfo;

public class PotionMap {
	// PotionID is the type (safezone, item, etc.)
	// Each PotionID has an iterable TreeMap with one entry per effect level
	//
	// This implementation allows only one status effect of each (type, level)
	// So you can have a level 0 regen from a safezone and a level 0 regen from an item,
	//   but not two level 0 regens from items
	private final EnumMap<PotionID, TreeMap<Integer, PotionInfo>> mPotionMap;

	// Type of this particular map
	private final PotionEffectType mType;

	private final boolean mIsNegative;

	public PotionMap(PotionEffectType type) {
		mPotionMap = new EnumMap<PotionID, TreeMap<Integer, PotionInfo>>(PotionID.class);
		mType = type;
		mIsNegative = PotionUtils.hasNegativeEffects(type);
	}

	private void addPotionMap(PotionID id, PotionInfo newPotionInfo) {
		Integer amplifier = newPotionInfo.mAmplifier;

		TreeMap<Integer, PotionInfo> trackedPotionInfo = mPotionMap.get(id);
		if (trackedPotionInfo == null) {
			trackedPotionInfo = new TreeMap<Integer, PotionInfo>();
		}

		if (mIsNegative) {
			// Negative potions don't track multiple levels - only the highest / longest one
			PotionInfo bestEffect = getBestEffect();

			// If the current "best" negative effect is less than this new one, track it
			// Make sure the last effect has had a chance to trigger before refreshing it
			if (bestEffect == null
			    || bestEffect.mAmplifier < newPotionInfo.mAmplifier
			    || (bestEffect.mAmplifier == newPotionInfo.mAmplifier
			        && bestEffect.mDuration < newPotionInfo.mDuration
			        && (newPotionInfo.mType != PotionEffectType.POISON
			            || newPotionInfo.mDuration - bestEffect.mDuration >= 25 / (bestEffect.mAmplifier + 1) + 1)
			        && (newPotionInfo.mType != PotionEffectType.WITHER
			            || newPotionInfo.mDuration - bestEffect.mDuration >= 40 / (bestEffect.mAmplifier + 1) + 1))) {
				trackedPotionInfo.put(amplifier, newPotionInfo);
			}

			// Remove all lower-level effects than this new one
			for (int i = newPotionInfo.mAmplifier - 1; i >= 0; i--) {
				trackedPotionInfo.remove(i);
			}
		} else {
			// Only add the new effect if it is longer for the same effect amplifier
			PotionInfo currentInfo = trackedPotionInfo.get(amplifier);
			if (currentInfo == null
			    || (currentInfo.mDuration < newPotionInfo.mDuration
			        && (currentInfo.mType != PotionEffectType.REGENERATION
			            || newPotionInfo.mDuration - currentInfo.mDuration >= 50 / (currentInfo.mAmplifier + 1) + 1))) {
				trackedPotionInfo.put(amplifier, newPotionInfo);
			}
		}

		mPotionMap.put(id, trackedPotionInfo);
	}

	public void addPotionMap(Player player, PotionID id, PotionInfo newPotionInfo) {
		addPotionMap(id, newPotionInfo);

		applyBestPotionEffect(player);
	}

	public void removePotionMap(Player player, PotionID id) {
		if (id == PotionID.ALL) {
			// Clear all effects from all sources
			mPotionMap.clear();
		} else {
			// Clear out all effects from this source
			mPotionMap.remove(id);
		}

		applyBestPotionEffect(player);
	}

	public void updatePotionStatus(Player player, int ticks) {
		//  First update the timers of all our tracked potion timers.
		boolean effectWoreOff = false;

		Iterator<Entry<PotionID, TreeMap<Integer, PotionInfo>>> potionIter = mPotionMap.entrySet().iterator();
		while (potionIter.hasNext()) {
			Entry<PotionID, TreeMap<Integer, PotionInfo>> potionMapping = potionIter.next();
			if (potionMapping != null) {
				TreeMap<Integer, PotionInfo> potionInfo = potionMapping.getValue();
				Iterator<Entry<Integer, PotionInfo>> potionInfoIter = potionInfo.entrySet().iterator();
				while (potionInfoIter.hasNext()) {
					PotionInfo info = potionInfoIter.next().getValue();

					info.mDuration -= ticks;
					if (info.mDuration <= 0) {
						effectWoreOff = true;
						potionInfoIter.remove();
					}
				}

				if (potionInfo.size() == 0) {
					potionIter.remove();
				}
			}
		}

		//  If a timer wears out, run another check to make sure the best potion effect is applied.
		if (effectWoreOff) {
			applyBestPotionEffect(player);
		}
	}

	private PotionInfo getBestEffect() {
		PotionInfo bestEffect = null;

		Iterator<Entry<PotionID, TreeMap<Integer, PotionInfo>>> potionSourceIter = mPotionMap.entrySet().iterator();
		while (potionSourceIter.hasNext()) {
			Entry<PotionID, TreeMap<Integer, PotionInfo>> potionInfo = potionSourceIter.next();

			for (Entry<Integer, PotionInfo> infoIter : potionInfo.getValue().entrySet()) {
				PotionInfo info = infoIter.getValue();

				if (bestEffect == null) {
					bestEffect = info;
				} else if (info.mAmplifier > bestEffect.mAmplifier) {
					bestEffect = info;
				} else if (info.mAmplifier == bestEffect.mAmplifier &&
				           info.mDuration > bestEffect.mDuration) {
					bestEffect = info;
				}
			}
		}

		return bestEffect;
	}

	private void applyBestPotionEffect(Player player) {
		PotionInfo bestEffect = getBestEffect();

		PotionEffect currentVanillaEffect = player.getPotionEffect(mType);
		if (currentVanillaEffect != null) {
			if (bestEffect == null
			    || currentVanillaEffect.getDuration() > (bestEffect.mDuration + 20)
			    || currentVanillaEffect.getDuration() < (bestEffect.mDuration - 20)
			    || bestEffect.mAmplifier != currentVanillaEffect.getAmplifier()) {

				// The current effect must be removed because the "best" effect is either less than it
				// OR the same strength but less duration
				player.removePotionEffect(mType);
			}
		}

		if (bestEffect != null) {
			// Effects over 100 "mask" all other effects of that type
			if (bestEffect.mAmplifier < 100) {
				PotionEffect effect = new PotionEffect(mType, bestEffect.mDuration, bestEffect.mAmplifier, bestEffect.mAmbient, bestEffect.mShowParticles);
				player.addPotionEffect(effect);
			}
		}
	}

	protected JsonObject getAsJsonObject() {
		JsonObject potionIDObject = null;
		JsonObject potionMapObject = new JsonObject();
		boolean hasMapping = false;

		Iterator<Entry<PotionID, TreeMap<Integer, PotionInfo>>> potionIter = mPotionMap.entrySet().iterator();
		while (potionIter.hasNext()) {
			Entry<PotionID, TreeMap<Integer, PotionInfo>> potionMapping = potionIter.next();
			if (potionMapping != null) {
				JsonArray effectListArray = new JsonArray();

				TreeMap<Integer, PotionInfo> potionInfo = potionMapping.getValue();
				Iterator<Entry<Integer, PotionInfo>> potionInfoIter = potionInfo.entrySet().iterator();
				while (potionInfoIter.hasNext()) {
					PotionInfo info = potionInfoIter.next().getValue();
					effectListArray.add(info.getAsJsonObject());
				}

				if (effectListArray.size() > 0) {
					potionMapObject.add(potionMapping.getKey().getName(), effectListArray);
					hasMapping = true;
				}
			}
		}

		if (hasMapping) {
			if (potionIDObject == null) {
				potionIDObject = new JsonObject();
			}

			potionIDObject.add("potion_map", potionMapObject);
		}

		return potionIDObject;
	}

	protected void loadFromJsonObject(JsonObject object) throws Exception {
		// Remove all current entries
		mPotionMap.clear();

		JsonObject potionMap = object.get("potion_map").getAsJsonObject();
		if (potionMap != null) {
			Set<Entry<String, JsonElement>> entries = potionMap.entrySet();
			for (Entry<String, JsonElement> entry : entries) {
				PotionID id = PotionID.getFromString(entry.getKey());
				if (id != null) {
					JsonArray potionInfoArray = entry.getValue().getAsJsonArray();

					Iterator<JsonElement> elementIter = potionInfoArray.iterator();
					while (elementIter.hasNext()) {
						JsonElement element = elementIter.next();

						PotionInfo info = new PotionInfo();
						info.loadFromJsonObject(element.getAsJsonObject());

						addPotionMap(id, info);
					}
				}
			}
		}
	}
}
