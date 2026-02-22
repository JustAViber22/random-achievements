package com.randomachievements.achievement;

import java.util.function.Predicate;

public record Achievement(
	String id,
	String displayName,
	String description,
	int targetCount,
	RewardType rewardType,
	Predicate<Object> progressFilter,
	boolean hidden
) {
	public static final int TOUCH_GRASS_TICKS = 60 * 20;

	public static final Achievement TOUCH_GRASS = new Achievement(
		"touch_grass",
		"Touch Grass",
		"Stand on grass for 60 seconds",
		TOUCH_GRASS_TICKS,
		RewardType.GRASS_BLOCKS,
		null,
		false
	);

	public static final Achievement COW_TIPPER = new Achievement(
		"cow_tipper",
		"Cow Tipper",
		"Punch 10 different cows",
		10,
		RewardType.RANDOM_COW_LOOT,
		null,
		false
	);

	public static final Achievement FAMOUS_VILLAGER = new Achievement(
		"famous_village",
		"Famous in the Village",
		"Right-click 15 villagers",
		15,
		RewardType.BREAD,
		null,
		false
	);

	public static final Achievement FISH_ADDICT = new Achievement(
		"fish_addict",
		"Fish Addict",
		"Catch 15 fish",
		15,
		RewardType.MORE_FISH,
		null,
		false
	);

	public static final Achievement WHAT_DO_I_DO = new Achievement(
		"what_do_i_do",
		"What Do I Do",
		"Type 'What do I do' in chat",
		1,
		RewardType.AXE_AND_PICK,
		null,
		false
	);

	public static final Achievement AWKWARD_POSITION = new Achievement(
		"awkward_position",
		"Awkwardly Positioned",
		"Have 69 or 420 in your X, Y, or Z coordinates",
		1,
		RewardType.AWKWARD_POTATOES,
		null,
		true
	);

	public static final Achievement SMOOTH_CRIMINAL = new Achievement(
		"smooth_criminal",
		"Smooth Criminal",
		"Steal from a villager",
		1,
		RewardType.SUSPICIOUS_STEW,
		null,
		true
	);

	public static final Achievement PACIFIST = new Achievement(
		"pacifist",
		"Pacifist",
		"Go 10 minutes without dealing any damage",
		1,
		RewardType.PEACE_FEATHER,
		null,
		false
	);

	public static final Achievement GO_TO_SLEEP = new Achievement(
		"go_to_sleep",
		"Go to Sleep Man",
		"Stay awake for 2 full nights in a row",
		1,
		RewardType.BED_SUPPLIES,
		null,
		false
	);

	public static final Achievement[] ALL = {
		TOUCH_GRASS, COW_TIPPER, FAMOUS_VILLAGER, FISH_ADDICT, WHAT_DO_I_DO,
		AWKWARD_POSITION, SMOOTH_CRIMINAL, PACIFIST, GO_TO_SLEEP
	};
}
