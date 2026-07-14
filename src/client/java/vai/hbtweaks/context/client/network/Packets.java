package vai.hbtweaks.context.client.network;

import fr.herobrine.network.AbstractPacket;
import fr.herobrine.network.PacketInfo;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class Packets {

    private Packets() {}

    public static <T extends AbstractPacket> CustomPacketPayload.Type<T> type(PacketInfo<T> info) {
        return new CustomPacketPayload.Type<>(info.identifier());
    }

    public static <T extends AbstractPacket> void registerC2S(PacketInfo<T> info) {
        PayloadTypeRegistry.serverboundPlay().register(type(info), info.streamCodec());
    }

    public static <T extends AbstractPacket> void registerS2C(PacketInfo<T> info) {
        PayloadTypeRegistry.clientboundPlay().register(type(info), info.streamCodec());
    }
}
