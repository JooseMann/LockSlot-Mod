package net.joosemann.lockslot.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

// Record describing a single player's lock data.
// Used C2S to check if the server has desynced and to resync if needed.
public record PlayerLockData(String uuid, List<LockInstance> locks) {

    // Base Codec, describing how to serialize data to send in a packet
    public static final Codec<PlayerLockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(PlayerLockData::uuid),
            LockInstance.CODEC.listOf().fieldOf("locks").forGetter(PlayerLockData::locks)
    ).apply(instance, PlayerLockData::new));

    // Stream codec derived from our codec that can be used to send data via a packet.
    public static final StreamCodec<ByteBuf, PlayerLockData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
