package net.joosemann.lockslot.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class PacketData {
    // Uses cases for a packet we send:
    //   - Locking a slot
    //   - Unlocking a slot
    //   - Checking for a desync
    //   - Directly updating data
    public enum PacketUse {
        LOCKING,
        UNLOCKING,
        CHECK_DESYNC,
        UPDATE_DATA
    }

    // Converts an integer to a PacketUse if possible, or a failed DataResult if not.
    private static DataResult<PacketUse> intToPacketUse(int num) {
        return switch (num) {
            case 0 -> DataResult.success(PacketUse.LOCKING);
            case 1 -> DataResult.success(PacketUse.UNLOCKING);
            case 2 -> DataResult.success(PacketUse.CHECK_DESYNC);
            case 3 -> DataResult.success(PacketUse.UPDATE_DATA);
            default -> DataResult.error(() -> "Invalid index " + num);
        };
    }

    // Converts a PacketUse enum to a corresponding integer.
    private static int packetUseToInt(PacketUse use) {
        return switch (use) {
            case PacketUse.LOCKING -> 0;
            case PacketUse.UNLOCKING -> 1;
            case PacketUse.CHECK_DESYNC -> 2;
            case PacketUse.UPDATE_DATA -> 3;
        };
    }

    // Codec for our PacketUse enum, based on the INT codec.
    // Use comapFlatMap() so that int -> enum is *not* always valid and enum -> int is always valid.
    public static Codec<PacketUse> PACKET_USE_CODEC = Codec.INT.comapFlatMap(
        PacketData::intToPacketUse, // Convert int to enum
        PacketData::packetUseToInt  // Convert enum to int
    );
}
