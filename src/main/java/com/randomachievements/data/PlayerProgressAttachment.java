package com.randomachievements.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.randomachievements.RandomAchievementsMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerProgressAttachment {

	public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.INT.optionalFieldOf("touchGrassTicks", 0).forGetter(PlayerProgress::touchGrassTicks),
			Codec.list(Codec.STRING.xmap(UUID::fromString, UUID::toString)).xmap(s -> new HashSet<>(s), s -> s.stream().toList())
				.optionalFieldOf("cowTipperCows", new HashSet<>()).forGetter(p -> new HashSet<>(p.cowTipperCows())),
			Codec.INT.optionalFieldOf("famousVillagerCount", 0).forGetter(PlayerProgress::famousVillagerCount),
			Codec.INT.optionalFieldOf("fishAddictCount", 0).forGetter(PlayerProgress::fishAddictCount),
			Codec.LONG.optionalFieldOf("lastDamageDealtTick", -1L).forGetter(PlayerProgress::lastDamageDealtTick),
			Codec.INT.optionalFieldOf("lastCheckedDay", -1).forGetter(PlayerProgress::lastCheckedDay),
			Codec.INT.optionalFieldOf("lastSleptDay", -1).forGetter(PlayerProgress::lastSleptDay),
			Codec.INT.optionalFieldOf("consecutiveNightsAwake", 0).forGetter(PlayerProgress::consecutiveNightsAwake),
			Codec.list(Codec.STRING).xmap(s -> new HashSet<>(s), s -> s.stream().toList())
				.optionalFieldOf("completed", new HashSet<>()).forGetter(p -> new HashSet<>(p.completed()))
		).apply(instance, PlayerProgress::new)
	);

	public static final AttachmentType<PlayerProgress> PROGRESS = AttachmentRegistry.create(
		Identifier.of(RandomAchievementsMod.MOD_ID, "achievement_progress"),
		builder -> builder
			.initializer(PlayerProgress::create)
			.persistent(CODEC)
			.copyOnDeath()
	);

	public static PlayerProgress get(Entity entity) {
		return entity.getAttachedOrElse(PROGRESS, PlayerProgress.create());
	}

	public static void set(Entity entity, PlayerProgress progress) {
		entity.setAttached(PROGRESS, progress);
	}

	public record PlayerProgress(
		int touchGrassTicks,
		Set<UUID> cowTipperCows,
		int famousVillagerCount,
		int fishAddictCount,
		long lastDamageDealtTick,
		int lastCheckedDay,
		int lastSleptDay,
		int consecutiveNightsAwake,
		Set<String> completed
	) {
		public static PlayerProgress create() {
			return new PlayerProgress(0, new HashSet<>(), 0, 0, -1L, -1, -1, 0, new HashSet<>());
		}

		public PlayerProgress withTouchGrassTicks(int v) {
			return new PlayerProgress(v, cowTipperCows, famousVillagerCount, fishAddictCount, lastDamageDealtTick, lastCheckedDay, lastSleptDay, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withCowTipperCows(Set<UUID> v) {
			return new PlayerProgress(touchGrassTicks, v, famousVillagerCount, fishAddictCount, lastDamageDealtTick, lastCheckedDay, lastSleptDay, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withFamousVillagerCount(int v) {
			return new PlayerProgress(touchGrassTicks, cowTipperCows, v, fishAddictCount, lastDamageDealtTick, lastCheckedDay, lastSleptDay, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withFishAddictCount(int v) {
			return new PlayerProgress(touchGrassTicks, cowTipperCows, famousVillagerCount, v, lastDamageDealtTick, lastCheckedDay, lastSleptDay, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withLastDamageDealtTick(long v) {
			return new PlayerProgress(touchGrassTicks, cowTipperCows, famousVillagerCount, fishAddictCount, v, lastCheckedDay, lastSleptDay, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withLastCheckedDay(int v) {
			return new PlayerProgress(touchGrassTicks, cowTipperCows, famousVillagerCount, fishAddictCount, lastDamageDealtTick, v, lastSleptDay, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withLastSleptDay(int v) {
			return new PlayerProgress(touchGrassTicks, cowTipperCows, famousVillagerCount, fishAddictCount, lastDamageDealtTick, lastCheckedDay, v, consecutiveNightsAwake, completed);
		}

		public PlayerProgress withConsecutiveNightsAwake(int v) {
			return new PlayerProgress(touchGrassTicks, cowTipperCows, famousVillagerCount, fishAddictCount, lastDamageDealtTick, lastCheckedDay, lastSleptDay, v, completed);
		}

		public PlayerProgress withCompleted(String id) {
			Set<String> c = new HashSet<>(completed);
			c.add(id);
			return new PlayerProgress(touchGrassTicks, cowTipperCows, famousVillagerCount, fishAddictCount, lastDamageDealtTick, lastCheckedDay, lastSleptDay, consecutiveNightsAwake, c);
		}
	}
}
