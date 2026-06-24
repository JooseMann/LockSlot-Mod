package net.joosemann.lockslot.networking.packets;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.LockInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

// Packet payload that sends lock information back and forth.
// Used as both a C2S and a S2C payload, so it not specifically marked as either.
public record LockDataPayload(List<LockInstance> lockedList) implements CustomPacketPayload {

    public static final Identifier LOCK_PAYLOAD_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "lock_data");
    public static final CustomPacketPayload.Type<LockDataPayload> ID = new CustomPacketPayload.Type<>(LOCK_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockDataPayload> STREAM_CODEC = StreamCodec.composite(LockInstance.STREAM_CODEC, LockDataPayload::lockedList, LockDataPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
