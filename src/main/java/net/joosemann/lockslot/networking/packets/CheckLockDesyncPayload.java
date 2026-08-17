package net.joosemann.lockslot.networking.packets;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.PlayerLockData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Packet holding a PlayerLockData to represent a lock data for the player given by data.uuid().
// Sent C2S periodically to ensure that the client and server have not desynced.
public record CheckLockDesyncPayload(PlayerLockData data) implements CustomPacketPayload {

    public static final Identifier CHECK_LOCK_DESYNC_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "check_desync");
    public static final CustomPacketPayload.Type<CheckLockDesyncPayload> ID = new CustomPacketPayload.Type<>(CHECK_LOCK_DESYNC_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CheckLockDesyncPayload> STREAM_CODEC = StreamCodec.composite(PlayerLockData.STREAM_CODEC, CheckLockDesyncPayload::data, CheckLockDesyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
