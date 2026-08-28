package net.joosemann.lockslot.networking.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.LockInstance;
import net.joosemann.lockslot.util.PacketData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

// Packet payload that sends a single LockInstance, as well as context for how to use this data.
// Sent C2S with possible PacketUses LOCKING or UNLOCKING, to keep the server updated on the client's lock data.
public record LockInstancePayload(LockInstance lock, PacketData.PacketUse use) implements CustomPacketPayload {

    public static final Identifier LOCK_INSTANCE_PAYLOAD_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "lock_instance");
    public static final CustomPacketPayload.Type<LockInstancePayload> ID = new CustomPacketPayload.Type<>(LOCK_INSTANCE_PAYLOAD_ID);
    private static final Codec<LockInstancePayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LockInstance.CODEC.fieldOf("lock").forGetter(LockInstancePayload::lock),
            PacketData.PACKET_USE_CODEC.fieldOf("use").forGetter(LockInstancePayload::use)
    ).apply(instance, LockInstancePayload::new)); // Only care about the base Codec to make the StreamCodec
    public static final StreamCodec<ByteBuf, LockInstancePayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
