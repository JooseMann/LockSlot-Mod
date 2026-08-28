package net.joosemann.lockslot.mixin;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.data.ServerLockedValues;
import net.joosemann.lockslot.util.LockInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    // Save our lock slot data on the player when everything else saves.
    @Inject(at = @At(value = "HEAD"), method = "save")
    protected void saveLockData(ServerPlayer serverPlayer, CallbackInfo ci) {
        // Check if we already have data attached.
        // If so, we want to overwrite it in favor of our new data.
        // Therefore, we remove the current data if it exists.
        if (serverPlayer.hasAttached(LockInstance.LOCK_ATTACHMENT_TYPE)) {
            serverPlayer.removeAttached(LockInstance.LOCK_ATTACHMENT_TYPE);
        }

        // Attach our current locked slots to the player, so they retain the information on the next session.
        // Do this for both the client-side and the server-side to account for singleplayer vs. multiplayer.
        if (server.isDedicatedServer()) {
            // Server-side
            Optional<LinkedList<LockInstance>> optionalList = ServerLockedValues.getLockedMapList(serverPlayer.getUUID());
            optionalList.ifPresent(lockInstances -> // Attach lock data if it is present
                    serverPlayer.setAttached(LockInstance.LOCK_ATTACHMENT_TYPE, lockInstances));
        }
        else {
            // Client-side
            serverPlayer.setAttached(LockInstance.LOCK_ATTACHMENT_TYPE, LockedValues.getLockedList());
        }

        LockSlot.LOGGER.info("Saved lock data for player {}", serverPlayer.nameAndId().name());
    }

}
