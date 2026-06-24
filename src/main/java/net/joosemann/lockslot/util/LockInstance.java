package net.joosemann.lockslot.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.joosemann.lockslot.LockSlot;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.LinkedList;
import java.util.List;

// Record that keeps track of the information for a single locked slot.
// The record on its own contains all of this information, as well as static variables to help with data persistence.
public record LockInstance(int index, int x, int y) {

    // Codec for use in serializing LockInstances, for use in saving and retrieving data from our persistent data.
    public static Codec<LockInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(LockInstance::index),
            Codec.INT.fieldOf("x").forGetter(LockInstance::x),
            Codec.INT.fieldOf("y").forGetter(LockInstance::y)
    ).apply(instance, LockInstance::new));

    // Stream codec to be used while sending packets containing data about LockInstances while using persistent data.
    // Sending this data around means sending all of our LockInstances, so we have a List<LockInstance> as our data type
    public static StreamCodec<ByteBuf, List<LockInstance>> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.listOf());

    // Data Attachment for all locked slots. Used to save and retrieve the data between sessions.
    // Like STREAM_CODEC, we want to save and retrieve all the data from one data attachment, so we attach a List<LockInstance>
    public static final AttachmentType<List<LockInstance>> LOCK_ATTACHMENT_TYPE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "lock_data"),
            builder -> builder
                    .initializer(LinkedList::new)
                    .persistent(LockInstance.CODEC.listOf())
                    .copyOnDeath()
                    .syncWith(
                            ByteBufCodecs.fromCodec(CODEC.listOf()),
                            AttachmentSyncPredicate.targetOnly()
                    )
    );
}
