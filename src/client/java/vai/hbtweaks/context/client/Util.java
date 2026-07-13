package vai.hbtweaks.context.client;

import com.mojang.authlib.properties.Property;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import vai.hbtweaks.context.client.keyboard.WritersBank;

public class Util {

    /** Distance tag shown next to a player's name ([:], [!], [+], [ ], [-], [#]). */
    public static MutableComponent distanceIndicator(double dist) {
        if (dist > 100f) return Component.literal("[:]").withStyle(ChatFormatting.WHITE);
        if (dist > 50f)  return Component.literal("[!]").withStyle(ChatFormatting.RED);
        if (dist > 20f)  return Component.literal("[+]").withStyle(ChatFormatting.YELLOW);
        if (dist > 10f)  return Component.literal("[ ]").withStyle(ChatFormatting.GREEN);
        if (dist > 3f)   return Component.literal("[-]").withStyle(ChatFormatting.DARK_GREEN);
        return Component.literal("[#]").withStyle(ChatFormatting.DARK_AQUA);
    }

    /** Animated 3-dot "is writing" indicator; grey dots when idle, "?" when never seen writing. */
    public static MutableComponent writingIndicator(Player target) {
        MutableComponent out = Component.empty();
        boolean writing = WritersBank.isWriting(target);
        int subtick = Minecraft.getInstance().gui.getGuiTicks() % 50;
        for (int i = 1; i <= 3; i++) {
            int v = 0x30;
            if (writing) {
                int level = Math.max(0, 8 - Math.abs(subtick - i * 10)); // evolution over 8 ticks, peak at i*10
                v = 0x30 + level * (0xFF - 0x30) / 8;
            }
            out.append(Component.literal("•").withColor(0xFF000000 | (v << 16) | (v << 8) | v));
        }
        if (!writing && !WritersBank.alreadyWrote(target))
            out.append(Component.literal("?").withColor(0xFF303030));
        return out;
    }

    public static String getMCName(Player player) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) return null;
        PlayerInfo pi = me.connection.getPlayerInfo(player.getUUID());
        if (pi == null) return null;
        return pi.getProfile().name();
    }

    public static Component getRpName(Player player) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) return null;
        PlayerInfo pi = me.connection.getPlayerInfo(player.getUUID());
        if (pi == null) return null;
        return pi.getTabListDisplayName();
    }


    public static String getFakeName(Player player) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) return null;
        PlayerInfo pi = me.connection.getPlayerInfo(player.getUUID());
        if (pi == null) return null;
        for (Property property : pi.getProfile().properties().get("minecraft_name"))
            return property.value();
        return null;
    }

    public static boolean hasFakeName(Player player) {
        return getFakeName(player) != null;
    }

    public static boolean isReal(Player player) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) return false;
        PlayerInfo pi = me.connection.getPlayerInfo(player.getUUID());
        if (pi == null) return false;
        return pi.getTabListDisplayName() != null && !pi.getTabListDisplayName().getString().isEmpty();
    }

    public static boolean hasDev() {
        return HBTweaksContextClient.DEBUG_MODE;
    }

    public static boolean hasPerm() {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) return false;
        return me.isCreative() || me.isSpectator() || hasDev();
    }
}
