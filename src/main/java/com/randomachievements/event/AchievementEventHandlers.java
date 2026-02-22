package com.randomachievements.event;

import com.randomachievements.achievement.Achievement;
import com.randomachievements.advancement.RandomAchievementsCriterion;
import com.randomachievements.data.PlayerProgressAttachment;
import com.randomachievements.network.SyncProgressS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.randomachievements.data.PlayerProgressAttachment.PlayerProgress;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AchievementEventHandlers {

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			long worldTime = world.getTime();
			int currentDay = (int) (worldTime / 24000L);

			world.getPlayers().forEach(player -> {
				if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
				PlayerProgress progress = PlayerProgressAttachment.get(serverPlayer);

				if (!progress.completed().contains(Achievement.TOUCH_GRASS.id())) {
					BlockPos below = serverPlayer.getBlockPos().down();
					if (world.getBlockState(below).isOf(Blocks.GRASS_BLOCK)) {
						int newTicks = progress.touchGrassTicks() + 1;
						if (newTicks >= Achievement.TOUCH_GRASS_TICKS) {
							complete(serverPlayer, Achievement.TOUCH_GRASS, progress
								.withTouchGrassTicks(0)
								.withCompleted(Achievement.TOUCH_GRASS.id()));
							progress = PlayerProgressAttachment.get(serverPlayer);
						} else {
							PlayerProgressAttachment.set(serverPlayer, progress.withTouchGrassTicks(newTicks));
							progress = progress.withTouchGrassTicks(newTicks);
						}
					} else if (progress.touchGrassTicks() > 0) {
						PlayerProgressAttachment.set(serverPlayer, progress.withTouchGrassTicks(0));
						progress = progress.withTouchGrassTicks(0);
					}
				}

				if (!progress.completed().contains(Achievement.AWKWARD_POSITION.id())) {
					BlockPos pos = serverPlayer.getBlockPos();
					int x = pos.getX(), y = pos.getY(), z = pos.getZ();
					if (x == 69 || x == 420 || y == 69 || y == 420 || z == 69 || z == 420) {
						complete(serverPlayer, Achievement.AWKWARD_POSITION, progress.withCompleted(Achievement.AWKWARD_POSITION.id()));
						progress = PlayerProgressAttachment.get(serverPlayer);
					}
				}

				if (!progress.completed().contains(Achievement.PACIFIST.id())) {
					long lastDamage = progress.lastDamageDealtTick();
					boolean eligible = lastDamage < 0 ? (worldTime >= 12000) : (worldTime - lastDamage >= 12000);
					if (eligible) {
						complete(serverPlayer, Achievement.PACIFIST, progress.withCompleted(Achievement.PACIFIST.id()));
						progress = PlayerProgressAttachment.get(serverPlayer);
					}
				}

				if (!progress.completed().contains(Achievement.GO_TO_SLEEP.id())) {
					int lastChecked = progress.lastCheckedDay();
					if (currentDay > lastChecked) {
						int lastSlept = progress.lastSleptDay();
						int consecutive = (lastSlept >= currentDay - 1) ? 0 : (progress.consecutiveNightsAwake() + 1);
						PlayerProgress updated = progress.withLastCheckedDay(currentDay).withConsecutiveNightsAwake(consecutive);
						if (consecutive >= 2) {
							complete(serverPlayer, Achievement.GO_TO_SLEEP, updated.withCompleted(Achievement.GO_TO_SLEEP.id()));
						} else {
							PlayerProgressAttachment.set(serverPlayer, updated);
						}
					}
				}
			});
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient()) return ActionResult.PASS;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

			PlayerProgress progress = PlayerProgressAttachment.get(serverPlayer);
			if (!progress.completed().contains(Achievement.PACIFIST.id())) {
				PlayerProgressAttachment.set(serverPlayer, progress.withLastDamageDealtTick(world.getTime()));
			}

			if (entity.getType() == EntityType.COW) {
				progress = PlayerProgressAttachment.get(serverPlayer);
				if (progress.completed().contains(Achievement.COW_TIPPER.id())) return ActionResult.PASS;

				var newSet = new java.util.HashSet<>(progress.cowTipperCows());
				newSet.add(entity.getUuid());
				PlayerProgress updated = progress.withCowTipperCows(newSet);
				if (newSet.size() >= Achievement.COW_TIPPER.targetCount()) {
					complete(serverPlayer, Achievement.COW_TIPPER, updated.withCompleted(Achievement.COW_TIPPER.id()));
				} else {
					PlayerProgressAttachment.set(serverPlayer, updated);
				}
			}
			return ActionResult.PASS;
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient()) return ActionResult.PASS;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

			if (entity instanceof VillagerEntity) {
				PlayerProgress progress = PlayerProgressAttachment.get(serverPlayer);
				if (progress.completed().contains(Achievement.FAMOUS_VILLAGER.id())) return ActionResult.PASS;

				int newCount = progress.famousVillagerCount() + 1;
				PlayerProgress updated = progress.withFamousVillagerCount(newCount);
				if (newCount >= Achievement.FAMOUS_VILLAGER.targetCount()) {
					complete(serverPlayer, Achievement.FAMOUS_VILLAGER, updated.withCompleted(Achievement.FAMOUS_VILLAGER.id()));
				} else {
					PlayerProgressAttachment.set(serverPlayer, updated);
				}
			}
			return ActionResult.PASS;
		});

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			String text = message.getSignedContent();
			if (text == null) return;
			if (!"what do i do".equals(text.trim().toLowerCase(java.util.Locale.ROOT))) return;
			PlayerProgress progress = PlayerProgressAttachment.get(sender);
			if (progress.completed().contains(Achievement.WHAT_DO_I_DO.id())) return;
			complete(sender, Achievement.WHAT_DO_I_DO, progress.withCompleted(Achievement.WHAT_DO_I_DO.id()));
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return;
			Block block = state.getBlock();
			if (block != Blocks.CHEST && block != Blocks.BARREL && block != Blocks.TRAPPED_CHEST) return;
			PlayerProgress progress = PlayerProgressAttachment.get(serverPlayer);
			if (progress.completed().contains(Achievement.SMOOTH_CRIMINAL.id())) return;
			Box box = new Box(pos.getX() - 48, pos.getY() - 48, pos.getZ() - 48, pos.getX() + 49, pos.getY() + 49, pos.getZ() + 49);
			if (!world.getEntitiesByClass(VillagerEntity.class, box, v -> true).isEmpty()) {
				complete(serverPlayer, Achievement.SMOOTH_CRIMINAL, progress.withCompleted(Achievement.SMOOTH_CRIMINAL.id()));
			}
		});

		EntitySleepEvents.START_SLEEPING.register((entity, pos) -> {
			if (!(entity instanceof ServerPlayerEntity serverPlayer)) return;
			World w = entity.getEntityWorld();
			if (w.isClient()) return;
			int day = (int) (w.getTime() / 24000L);
			PlayerProgress progress = PlayerProgressAttachment.get(serverPlayer);
			PlayerProgressAttachment.set(serverPlayer, progress.withLastSleptDay(day));
		});
	}

	public static boolean grantAchievement(ServerPlayerEntity player, Achievement achievement) {
		PlayerProgress progress = PlayerProgressAttachment.get(player);
		if (progress.completed().contains(achievement.id())) return false;
		complete(player, achievement, progress.withCompleted(achievement.id()));
		return true;
	}

	private static void complete(ServerPlayerEntity player, Achievement achievement, PlayerProgress newProgress) {
		PlayerProgressAttachment.set(player, newProgress);
		achievement.rewardType().give(player);
		RandomAchievementsCriterion.INSTANCE.trigger(player, achievement.id());
		var world = player.getEntityWorld();
		if (world instanceof ServerWorld serverWorld) {
			var server = serverWorld.getServer();
			var loader = server.getAdvancementLoader();
			var advancementId = Identifier.of(com.randomachievements.RandomAchievementsMod.MOD_ID, achievement.id());
			var entry = loader.get(advancementId);
			if (entry != null) {
				var tracker = server.getPlayerManager().getAdvancementTracker(player);
				tracker.grantCriterion(entry, "completed");
			}
		}
		player.sendMessage(Text.literal("§6§lNew Random Achievement! §r§f" + achievement.displayName() + " §7- §f" + achievement.description()), false);
		ServerPlayNetworking.send(player, new SyncProgressS2CPayload(new java.util.ArrayList<>(newProgress.completed()), achievement.id()));
	}
}
