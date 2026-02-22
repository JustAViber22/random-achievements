package com.randomachievements.network;

import com.randomachievements.RandomAchievementsMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record SyncProgressS2CPayload(List<String> completedIds, String lastGrantedId) implements CustomPayload {

	public static final CustomPayload.Id<SyncProgressS2CPayload> ID =
		new CustomPayload.Id<>(net.minecraft.util.Identifier.of(RandomAchievementsMod.MOD_ID, "sync_progress"));

	public static final PacketCodec<RegistryByteBuf, SyncProgressS2CPayload> CODEC = PacketCodec.of(
		SyncProgressS2CPayload::write,
		SyncProgressS2CPayload::read
	);

	private static void write(SyncProgressS2CPayload payload, RegistryByteBuf buf) {
		buf.writeVarInt(payload.completedIds().size());
		for (String id : payload.completedIds()) {
			buf.writeString(id);
		}
		buf.writeBoolean(payload.lastGrantedId() != null);
		if (payload.lastGrantedId() != null) {
			buf.writeString(payload.lastGrantedId());
		}
	}

	private static SyncProgressS2CPayload read(RegistryByteBuf buf) {
		int n = buf.readVarInt();
		List<String> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			list.add(buf.readString());
		}
		String lastGrantedId = buf.readBoolean() ? buf.readString() : null;
		return new SyncProgressS2CPayload(list, lastGrantedId);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
