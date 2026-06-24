package net.joosemann.lockslot.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.joosemann.lockslot.networking.packets.AskForDataS2CPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    // Save our lock slot data on the player when everything else saves.
    @Inject(at = @At(value = "HEAD"), method = "save")
    protected void saveLockData(ServerPlayer serverPlayer, CallbackInfo ci) {
        // We want to save our current lock data to the player as a data attachment.
        // However, we don't have access to that current data, as it is located on the client-side,
        // while we are on the server-side. Therefore, send a packet to the client to ask it for the data.
        AskForDataS2CPayload payload = new AskForDataS2CPayload(true); // Boolean is a dummy variable
        ServerPlayNetworking.send(serverPlayer, payload); // Sends to networking.handlers.ClientNetworkHandlers
    }

}
