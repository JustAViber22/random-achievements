package com.randomachievements;

import com.randomachievements.advancement.RandomAchievementsCriterion;
import com.randomachievements.data.PlayerProgressAttachment;
import com.randomachievements.event.AchievementEventHandlers;
import com.randomachievements.network.SyncProgressS2CPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAchievementsMod implements ModInitializer {
	public static final String MOD_ID = "randomachievements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Criteria.register(RandomAchievementsMod.MOD_ID + ":complete", RandomAchievementsCriterion.INSTANCE);
		PlayerProgressAttachment.PROGRESS.getClass();
		AchievementEventHandlers.register();

		PayloadTypeRegistry.playS2C().register(SyncProgressS2CPayload.ID, SyncProgressS2CPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var progress = PlayerProgressAttachment.get(handler.player);
			ServerPlayNetworking.send(handler.player, new SyncProgressS2CPayload(new java.util.ArrayList<>(progress.completed()), null));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("ra")
				.executes(ctx -> {
					ctx.getSource().sendFeedback(() -> Text.literal("Random Achievements. Use /ra progress or press J to see your progress."), false);
					return 1;
				}));
		});

		LOGGER.info("Random Achievements loaded. Go touch grass.");
	}
}
