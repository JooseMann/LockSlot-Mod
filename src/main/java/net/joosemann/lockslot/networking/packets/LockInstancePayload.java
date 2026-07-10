package net.joosemann.lockslot.networking.packets;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.LockInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

// Packet payload that sends a single LockInstance.
// Sent C2S to keep the server updated on whenever the client updates its locks.
public record LockInstancePayload(LockInstance lock) implements CustomPacketPayload {

    public static final Identifier LOCK_INSTANCE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "lock_instance");
    public static final CustomPacketPayload.Type<LockInstancePayload> ID = new CustomPacketPayload.Type<>(LOCK_INSTANCE_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockInstancePayload> STREAM_CODEC = StreamCodec.composite(LockInstance.STREAM_CODEC, LockInstancePayload::lock, LockInstancePayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
