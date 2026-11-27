package net.joosemann.lockslot.networking.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.PacketData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Small C2S packet that notifies and updates a multiplayer server of a player's config change.
 * In particular, this packet handles a boolean setting.
 *
 * @param newValue The new boolean config setting.
 */
public record ModConfigPayload(boolean newValue, PacketData.PacketUse packetUse) implements CustomPacketPayload {
    // Packet ID
    public static final Identifier MOD_CONFIG_UPDATE_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "config_update");
    // Type of packet, needed whenever registering network handlers to do something when this packet is sent
    public static final CustomPacketPayload.Type<ModConfigPayload> ID = new CustomPacketPayload.Type<>(MOD_CONFIG_UPDATE_ID);
    // Private Codec, only used to serialize data via the StreamCodec.
    private static final Codec<ModConfigPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("newValue").forGetter(ModConfigPayload::newValue),
            PacketData.PACKET_USE_CODEC.fieldOf("packetUse").forGetter(ModConfigPayload::packetUse)
    ).apply(instance, ModConfigPayload::new));
    // StreamCodec to tell Minecraft how to serialize/deserialize this packet when sending it C2S/S2C.
    public static final StreamCodec<ByteBuf, ModConfigPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
