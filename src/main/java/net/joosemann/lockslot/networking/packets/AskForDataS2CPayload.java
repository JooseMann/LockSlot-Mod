package net.joosemann.lockslot.networking.packets;

import net.joosemann.lockslot.LockSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

// Payload used to ask the client for lock data.
// This is needed as a side effect of data being saved on the server-side, but (for us) stored on the client-side.
public record AskForDataS2CPayload(boolean dummy) implements CustomPacketPayload {

    public static final Identifier ASK_FOR_DATA_ID = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "ask_for_data");
    public static final CustomPacketPayload.Type<AskForDataS2CPayload> ID = new CustomPacketPayload.Type<>(ASK_FOR_DATA_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AskForDataS2CPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, AskForDataS2CPayload::dummy, AskForDataS2CPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
