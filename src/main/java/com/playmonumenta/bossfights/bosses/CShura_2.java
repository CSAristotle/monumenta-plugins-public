package com.playmonumenta.bossfights.bosses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.playmonumenta.bossfights.BossBarManager;
import com.playmonumenta.bossfights.BossBarManager.BossHealthAction;
import com.playmonumenta.bossfights.Plugin;
import com.playmonumenta.bossfights.SpellManager;
import com.playmonumenta.bossfights.spells.Spell;
import com.playmonumenta.bossfights.spells.SpellBlockBreak;
import com.playmonumenta.bossfights.spells.SpellConditionalTeleport;
import com.playmonumenta.bossfights.spells.SpellSmokeBomb;
import com.playmonumenta.bossfights.spells.SpellTpBehindRandomPlayer;
import com.playmonumenta.bossfights.utils.SerializationUtils;
import com.playmonumenta.bossfights.utils.Utils;

public class CShura_2 extends BossAbilityGroup {
	public static final String identityTag = "boss_cshura_2";
	public static final int detectionRange = 50;
	private final Random mRand = new Random();

	private final LivingEntity mBoss;
	private final Location mSpawnLoc;
	private final Location mEndLoc;


	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new CShura_2(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public CShura_2(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		mBoss = boss;
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;

		mBoss.addScoreboardTag("Boss");

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellTpBehindRandomPlayer(plugin, mBoss, 160),
			new SpellSmokeBomb(plugin, boss, 7, 40)
		));
		List<Spell> passiveSpells = Arrays.asList(
			new SpellBlockBreak(mBoss),
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> spawnLoc.distance(b.getLocation()) > 35)
		);
		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();
		events.put(50, mBoss -> {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Utils.getExecuteCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[\",\"color\":\"gold\"},{\"text\":\"C'Shura\",\"color\":\"dark_red\",\"bold\":true},{\"text\":\"] \",\"color\":\"gold\",\"bold\":false},{\"text\":\"Z'CUN, DIE ALREADY!\",\"color\":\"red\"}]"));
			mBoss.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 12000, 0), true);
			mBoss.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12000, 0), true);
			for (Player player : Utils.playersInRange(mBoss.getLocation(), 5)) {
				Vector dir = player.getLocation().subtract(mBoss.getLocation().toVector()).toVector().multiply(1.0f);
				dir.setY(0.5f);

				player.setVelocity(dir);
			}
		});

		BossBarManager bossBar = new BossBarManager(mBoss, detectionRange, BarColor.RED, BarStyle.SOLID, events);

		super.constructBoss(plugin, identityTag, mBoss, activeSpells, passiveSpells, detectionRange, bossBar);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int player_count = Utils.playersInRange(mBoss.getLocation(), detectionRange).size();
		int hp_del = 512;
		int armor = (int)(Math.sqrt(player_count * 2) - 1);
		while (player_count > 0) {
			bossTargetHp = bossTargetHp + hp_del;
			hp_del = hp_del / 2;
			player_count--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);

		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "effect @s minecraft:blindness 2 2");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"C'Shura\",\"color\":\"dark_green\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"The Soulbinder\",\"color\":\"green\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 0.7");
	}
	public void bossDamagedEntity(EntityDamageByEntityEvent event) {
		int rand = mRand.nextInt(4);
		LivingEntity target = (LivingEntity) event.getEntity();
		if (rand == 0) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0, false, true));
		} else if (rand == 1) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, true));
		} else if (rand == 2) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, false, true));
		} else if (rand == 3) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 0, false, true));
		}
	}

	@Override
	public void death() {
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}
}