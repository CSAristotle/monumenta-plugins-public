package com.playmonumenta.plugins.classes;

public enum Spells {

	//Mage

	//--Core Abilities
	MANA_LANCE("Mana Lance"),
	FROST_NOVA("Frost Nova"),
	PRISMATIC_SHIELD("Prismatic Shield"),
	MAGMA_SHIELD("Magma Shield"),
	ARCANE_STRIKE("Arcane Strike"),
	ELEMENTAL_ARROWS("Elemental Arrows"),

	//--Elementalist Spec.
	BLIZZARD("Blizzard"),
	STARFALL("Starfall"),
	ELEMENTAL_SPIRIT("Elemental Spirit"),

	//--Arcanist Spec.
	FSWORD("Flash Sword"),

	//Rogue

	//--Core Abilities
	BY_MY_BLADE("By My Blade"),
	ADVANCING_SHADOWS("Advancing Shadows"),
	DODGING("Dodging"),
	ESCAPE_DEATH("Escape Death"),
	SMOKESCREEN("Smokescreen"),
	DAGGER_THROW("Dagger Throw"),

	//--Swordsage Spec.
	SNAKE_HEAD("Snake Head"),
	BLADE_SURGE("Blade Surge"),
	BLADE_DANCE("Blade Dance"),
	WIND_WALK("Wind Walk"),

	//--Assassin Spec.
	BODKIN_BLITZ("Bodkin Blitz"),
	CLOAK_AND_DAGGER("Cloak And Dagger"),

	//Cleric

	//--Core Abilities
	CELESTIAL_BLESSING("Celestial Blessing"),
	CLEANSING("Cleansing Rain"),
	HEALING("Hand of Light"),

	//--Pally
	HOLY_JAVELIN("Holy Javelin"),
	CHOIR_BELLS("Choir Bells"),
	LUMINOUS_INFUSION("Luminous Infusion"),

	//--Hierophant
	HALLOWED_BEAM("Hallowed Beam"),
	THURIBLE_PROCESSION("Thurible Procession"),
	ENCHANTED_PRAYER("Enchanted Prayer"),

	//Scout

	//-- Core Abilities
	VOLLEY("Volley"),
	EAGLE_EYE("Eagle Eye"),

	//--Camper/Sniper
	ENCHANTED_ARROW("Enchanted Arrow"),
	SPLIT_ARROW("Split Arrow"),

	//--Ranger
	QUICKDRAW("Quickdraw"),
	DISENGAGE("Disengage"),
	PRECISION_STRIKE("Precision Strike"),

	//Warlock

	//--Core Abilities
	AMPLIFYING("Amplifying Hex"),
	BLASPHEMY("Blasphemous Aura"),
	SOUL_REND("Soul Rend"),
	CONSUMING_FLAMES("Consuming Flames"),
	GRASPING_CLAWS("Grasping Claws"),
	EXORCISM("Exorcism"),

	//--Reaper
	HUNGERING_VORTEX("Hungering Vortex"),
	DEATHS_TOUCH("Death's Touch"),
	DARK_PACT("Dark Pact"),

	//--Tenebrist
	FRACTAL_ENERVATION("Fractal Enervation"),
	WITHERING_GAZE("Withering Gaze"),

	//Warrior

	//--Core Abilites
	RIPOSTE("Riposte"),
	DEFENSIVE_LINE("Defensive Line"),
	COUNTER_STRIKE("Counter Strike"),
	SHIELD_BASH("Shield Bash"),

	//--Berserker
	METEOR_SLAM("Meteor Slam"),

	//--Guardian
	SHIELD_WALL("Shield Wall"),
	CHALLENGE("Challenge"),
	BODYGUARD("Bodyguard"),

	//Alchemist
	POWER_INJECTION("Power Injection"),
	IRON_TINCTURE("Iron Tincture"),
	ENFEEBLING_ELIXIR("Enfeebling Elixir"),
	UNSTABLE_ARROWS("Unstable Arrows"),
	ALCHEMICAL_ARTILLERY("Alchemical Artillery"),

	//Harbinger
	ADRENAL_SERUM("Adrenal Serum"),
	PURPLE_HAZE("Purple Haze"),

	//Apothecary
	ALCHEMICAL_AMALGAM("Alchemical Amalgam");

	private final String mName;

	Spells(String name) {
		this.mName = name;
	}

	public String getName() {
		return mName;
	}
}
