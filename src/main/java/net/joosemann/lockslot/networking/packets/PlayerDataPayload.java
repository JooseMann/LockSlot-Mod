package net.joosemann.lockslot.networking.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.util.PacketData;
import net.joosemann.lockslot.util.PlayerLockData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Packet holding a PlayerLockData to represent a lock data for the player given by data.getFirst().uuid().
// Sent C2S periodically to ensure that the client and server have not desynced and to update data.
public record PlayerDataPayload(PlayerLockData data, PacketData.PacketUse packetUse) implements CustomPacketPayload {

    public static final Identifier PLAYER_DATA_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "player_data");
    public static final CustomPacketPayload.Type<PlayerDataPayload> ID = new CustomPacketPayload.Type<>(PLAYER_DATA_ID);
    private static final Codec<PlayerDataPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerLockData.CODEC.fieldOf("data").forGetter(PlayerDataPayload::data),
            PacketData.PACKET_USE_CODEC.fieldOf("packetUse").forGetter(PlayerDataPayload::packetUse)
    ).apply(instance, PlayerDataPayload::new)); // Only care about the base Codec to make the StreamCodec
    public static final StreamCodec<ByteBuf, PlayerDataPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
