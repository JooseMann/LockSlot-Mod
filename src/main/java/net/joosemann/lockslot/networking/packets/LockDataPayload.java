package net.joosemann.lockslot.networking.packets;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.LockInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

// Packet payload that sends lock information back and forth.
// Used as a S2C payload for updating an entire list of locked slots at once (such as when logging into a server).
// This packet is only used for this one purpose, so does not also contain a PacketData.PacketUse.
public record LockDataPayload(List<LockInstance> lockedList) implements CustomPacketPayload {

    public static final Identifier LOCK_PAYLOAD_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "lock_data");
    public static final CustomPacketPayload.Type<LockDataPayload> ID = new CustomPacketPayload.Type<>(LOCK_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockDataPayload> STREAM_CODEC = StreamCodec.composite(LockInstance.LIST_STREAM_CODEC, LockDataPayload::lockedList, LockDataPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
