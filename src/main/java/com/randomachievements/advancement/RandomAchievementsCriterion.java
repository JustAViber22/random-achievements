package com.randomachievements.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class RandomAchievementsCriterion extends AbstractCriterion<RandomAchievementsCriterion.Conditions> {

	public static final RandomAchievementsCriterion INSTANCE = new RandomAchievementsCriterion();

	@Override
	public Codec<Conditions> getConditionsCodec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayerEntity player, String achievementId) {
		trigger(player, conditions -> conditions.matches(achievementId));
	}

	public record Conditions(Optional<LootContextPredicate> player, Optional<String> achievementId)
		implements AbstractCriterion.Conditions {

		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				Codec.STRING.optionalFieldOf("achievement_id").forGetter(Conditions::achievementId)
			).apply(instance, Conditions::new)
		);

		public boolean matches(String triggeredId) {
			return achievementId.isEmpty() || achievementId.get().equals(triggeredId);
		}
	}
}
