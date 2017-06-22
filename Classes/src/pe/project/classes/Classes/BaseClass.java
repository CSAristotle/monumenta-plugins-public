package pe.project.classes.Classes;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import pe.project.classes.Main;
import pe.project.classes.Timers.CooldownTimers;

public class BaseClass {
	protected Random mRandom;
	protected Main mPlugin;
	
	static public int FAKE_COOLDOWN_ID = 9999;

	protected CooldownTimers mCooldowns = null;
	
	public BaseClass(Main plugin, Random random) {
		mRandom = random;
		mPlugin = plugin;
		
		mCooldowns = new CooldownTimers(plugin);
	}
	
	public void FakeAbilityOffCooldown(Player player, int abilityID) {
		if (abilityID == ClericClass.CELESTIAL_1_FAKE_ID) {
			player.removeMetadata(ClericClass.CELESTIAL_1_TAGNAME, mPlugin);
		} else if (abilityID == ClericClass.CELESTIAL_2_FAKE_ID) {
			player.removeMetadata(ClericClass.CELESTIAL_2_TAGNAME, mPlugin);
		} else if (abilityID == ScoutClass.STANDARD_BEARER_FAKE_ID) {
			player.removeMetadata(ScoutClass.STANDARD_BEARER_TAG_NAME, mPlugin);
		}
	}
	
	public void AbilityOffCooldown(Player player, int abilityID) {
	}
	
	public void PulseEffectApplyEffect(Player owner, Location loc, Player effectedPlayer, int abilityID) {
	}
	
	public void PulseEffectRemoveEffect(Player owner, Location loc, Player effectedPlayer, int abilityID) {
	}
	
	public boolean has1SecondTrigger() {
		return false;
	}
	
	public boolean has2SecondTrigger() {
		return false;
	}
	
	public boolean has40SecondTrigger() {
		return false;
	}
	
	public boolean has60SecondTrigger() {
		return false;
	}
	
	public void PeriodicTrigger(Player player, boolean twoSeconds, boolean fourtySeconds, boolean sixtySeconds, int originalTime) {
	}
	
	public void ModifyDamage(Player player, BaseClass owner, EntityDamageByEntityEvent event) {
	}
	
	public void PlayerDamagedByLivingEntityEvent(Player player, LivingEntity damager, double damage) {
	}
	
	public boolean LivingEntityDamagedByPlayerEvent(Player player, LivingEntity damagee, double damage) {
		return true;
	}
	
	public void LivingEntityShotByPlayerEvent(Player player, Arrow arrow, LivingEntity damagee, EntityDamageByEntityEvent event) {
	}
	
	public void PlayerShotArrowEvent(Player player, Arrow arrow) {
	}
	
	public void PlayerThrewSplashPotionEvent(Player player, SplashPotion potion) {
	}
	
	public void ProjectileHitEvent(Player player, Arrow arrow) {
	}
	
	public void PlayerItemHeldEvent(Player player) {
	}
	
	public void PlayerDropItemEvent(Player player) {
	}
	
	public void PlayerItemBreakEvent(Player player) {
	}
	
	public void PlayerRespawnEvent(Player player) {
	}
	
	public void EntityDeathEvent(Player player, LivingEntity killedEntity) {
	}
	
	public void PlayerInteractEvent(Player player, Action action, Material material) {
	}
	
	public boolean PlayerSplashPotionEvent(Player player, Collection<LivingEntity> affectedEntities, ThrownPotion potion) {
		return true;
	}
	
	public void AreaEffectCloudApplyEvent(List<LivingEntity> entities, Player player) {
	}
}
