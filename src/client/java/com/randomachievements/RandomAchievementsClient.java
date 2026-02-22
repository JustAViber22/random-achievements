package com.randomachievements;

import com.randomachievements.achievement.Achievement;
import com.randomachievements.client.ClientProgress;
import com.randomachievements.network.SyncProgressS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.List;

public class RandomAchievementsClient implements ClientModInitializer {

	public static KeyBinding OPEN_TRACKER_KEY;

	private static boolean hasShownTrackerHint = false;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncProgressS2CPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				ClientProgress.setCompleted(payload.completedIds());
				showTrackerHintOnce(context.client());
				if (payload.lastGrantedId() != null) {
					showAdvancementToast(context.client(), payload.lastGrantedId());
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> hasShownTrackerHint = false);

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(RandomAchievementsMod.MOD_ID, "category"));
		OPEN_TRACKER_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key." + RandomAchievementsMod.MOD_ID + ".open_tracker",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_J,
			category
		));
		moveKeyBindingCategoryToTop(category);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_TRACKER_KEY.wasPressed()) {
				if (client.player != null) sendProgressToChat(client);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				ClientCommandManager.literal("ra")
					.then(ClientCommandManager.literal("progress")
						.executes(context -> {
							MinecraftClient client = context.getSource().getClient();
							if (client.player != null) sendProgressToChat(client);
							return 1;
						}))
			);
		});
	}

	private static void showAdvancementToast(MinecraftClient client, String achievementId) {
		var toastManager = client.getToastManager();
		if (toastManager == null) return;

		String displayName = null;
		for (Achievement a : Achievement.ALL) {
			if (a.id().equals(achievementId)) {
				displayName = a.displayName();
				break;
			}
		}
		if (displayName == null) displayName = achievementId;

		var handler = client.getNetworkHandler();
		if (handler != null) {
			var advancementHandler = handler.getAdvancementHandler();
			if (advancementHandler != null) {
				var id = Identifier.of(RandomAchievementsMod.MOD_ID, achievementId);
				var entry = advancementHandler.get(id);
				if (entry != null) {
					toastManager.add(new AdvancementToast(entry));
					return;
				}
			}
		}
		SystemToast.add(toastManager, new SystemToast.Type(5000L), Text.literal("New Random Achievement!"), Text.literal(displayName));
	}

	private static void sendProgressToChat(MinecraftClient client) {
		if (client.inGameHud == null) return;
		var chat = client.inGameHud.getChatHud();
		chat.addMessage(Text.literal("§6§lRandom Achievements"));
		for (Achievement a : Achievement.ALL) {
			boolean done = ClientProgress.getCompleted().contains(a.id());
			String desc = (a.hidden() && !done) ? "???" : a.description();
			String line = (done ? "§a✓ " : "§7○ ") + a.displayName() + " §8- " + desc;
			chat.addMessage(Text.literal(line));
		}
		chat.addMessage(Text.literal("§7Press J or use /ra progress to show this again."));
		chat.addMessage(Text.literal("§8Scroll up to see full list."));
	}

	@SuppressWarnings("unchecked")
	private static void moveKeyBindingCategoryToTop(KeyBinding.Category category) {
		try {
			for (Field f : KeyBinding.Category.class.getDeclaredFields()) {
				if (List.class.equals(f.getType()) && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
					f.setAccessible(true);
					List<KeyBinding.Category> categories = (List<KeyBinding.Category>) f.get(null);
					if (categories != null && categories.remove(category)) {
						categories.add(0, category);
					}
					return;
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static void showTrackerHintOnce(net.minecraft.client.MinecraftClient client) {
		if (hasShownTrackerHint || client.inGameHud == null || OPEN_TRACKER_KEY == null) return;
		hasShownTrackerHint = true;
		Text message = Text.literal("Random Achievements - Press ")
			.append(OPEN_TRACKER_KEY.getBoundKeyLocalizedText())
			.append(Text.literal(" to track your useless progress!"));
		client.inGameHud.getChatHud().addMessage(message);
	}
}
