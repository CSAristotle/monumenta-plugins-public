package com.playmonumenta.plugins.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BossAbilityDamageEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean isCancelled;
	private LivingEntity mBoss;
	private LivingEntity mDamaged;
	private double mDamage;

	public BossAbilityDamageEvent(LivingEntity boss, LivingEntity damaged, double damage) {
		mBoss = boss;
		mDamaged = damaged;
		mDamage = damage;
	}

	public LivingEntity getBoss() {
		return mBoss;
	}

	public LivingEntity getDamaged() {
		return mDamaged;
	}

	public void setDamage(double damage) {
		mDamage = damage;
	}

	public double getDamage() {
		return mDamage;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.isCancelled = arg0;
	}

	// Mandatory Event Methods (If you remove these, I'm 99% sure the event will break)

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
