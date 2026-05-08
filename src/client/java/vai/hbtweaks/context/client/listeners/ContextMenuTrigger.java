package vai.hbtweaks.context.client.listeners;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import vai.hbtweaks.context.HBTweaksContext;
import vai.hbtweaks.context.client.contextmenu.ContextMenu;
import vai.hbtweaks.context.client.contextmenu.CustomContextMenuLoader;
import vai.mousepointerapi.events.MouseTrackerEntityClickUpCallback;
import vai.mousepointerapi.util.ClickType;
import vai.mousepointerapi.util.ScreenType;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ContextMenuTrigger implements MouseTrackerEntityClickUpCallback, ScreenMouseEvents.AfterMouseClick
{

    /* TODO
        fake name must be handled
        Hover with the name
        Hide behind a block
     */

    private static ContextMenu contextMenu = null;
    public static Map<String, Object> customMenu = CustomContextMenuLoader.readYaml(Paths.get("custom_menu.yml"));

    private static final ResourceLocation menuResourceLocation = ResourceLocation.fromNamespaceAndPath(HBTweaksContext.MOD_ID, "before_chat");

    public ContextMenuTrigger() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                ContextMenuTrigger.menuResourceLocation,
                ContextMenuTrigger::render);
    }

    private boolean isCommandAvailable(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return mc.player.connection.getCommands().getRoot().getChild(command) != null;
    }

    public static String getMCName(Player player) {
        LocalPlayer mainPlayer = Minecraft.getInstance().player;
        if (mainPlayer != null) {
            PlayerInfo pi = mainPlayer.connection.getPlayerInfo(player.getUUID());
            if (pi != null)
                return pi.getProfile().getName();
        }
        return null;
    }

    private ContextMenu makeReputContextMenu(Player player) {
        return makeReputAvisContextMenu(player, "reput");
    }

    private ContextMenu makeAvisContextMenu(Player player) {
        return makeReputAvisContextMenu(player, "avis");
    }

    private ContextMenu makeReputAvisContextMenu(Player player, String cmdPart) {
        String mcName = ContextMenuTrigger.getMCName(player);
        ContextMenu context = new ContextMenu(0, 0, player);
        context.addCommandItem(Component.literal("✔ OK").withStyle(ChatFormatting.GREEN), cmdPart + "ok %mcname%");
        context.addCommandItem(Component.literal("• NO").withStyle(ChatFormatting.GRAY), cmdPart + "no %mcname%");
        context.addCommandItem(Component.literal("✘ KO").withStyle(ChatFormatting.RED), cmdPart + "ko %mcname%");
        return context;
    }

    private ContextMenu makeDebugContextMenu(Player player) {
        ContextMenu context = new ContextMenu(0, 0, player);
        PlayerInfo pi = Minecraft.getInstance().player.connection.getPlayerInfo(player.getUUID());
        if (pi == null)
            return null;
        PropertyMap properties = pi.getProfile().getProperties();
        for (Map.Entry<String, Property> e : properties.entries()) {
            context.addActionItem(Component.literal(e.getKey()), () -> {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(e.getValue().name()));
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(e.getValue().value()));
            });
        }
        return context;
    }

    private ContextMenu makeInfoContextMenu(Player player) {
        PlayerInfo pi = Minecraft.getInstance().player.connection.getPlayerInfo(player.getUUID());
        if (pi == null)
            return null;
        ContextMenu infosContext = new ContextMenu(0, 0, player);
        infosContext.addInfoItem(pi.getTabListDisplayName());
        try {
            Component mcName = Component.literal(ContextMenuTrigger.getMCName(player)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            infosContext.addInfoItem(mcName);
        } catch (Exception ignored) { }
        return infosContext;
    }

    @Override
    public void onClickUp(List<Entity> list, ClickType clickType, ScreenType screenType) {
        if (screenType == ScreenType.CHAT && clickType == ClickType.RIGHT_CLICK) {
            Minecraft mc = Minecraft.getInstance();
            Entity target = list.getFirst();
            if (!(target instanceof Player))
                return;
            Player targetPlayer = (Player) list.getFirst();
            if (mc.player.connection.getPlayerInfo(targetPlayer.getUUID()) == null)
                return;
            int x = (int) mc.mouseHandler.getScaledXPos(mc.getWindow());
            int y = (int) mc.mouseHandler.getScaledYPos(mc.getWindow());

            if (ContextMenuTrigger.contextMenu != null) {
                ContextMenuTrigger.contextMenu.close();
            }

            ContextMenuTrigger.contextMenu = new ContextMenu(x, y, targetPlayer);
            ContextMenuTrigger.contextMenu.addSubmenuItem("Infos", makeInfoContextMenu(targetPlayer));
            if (isCommandAvailable("reputok"))
                ContextMenuTrigger.contextMenu.addSubmenuItem("Reput", makeReputContextMenu(targetPlayer));
            if (isCommandAvailable("avisok"))
                ContextMenuTrigger.contextMenu.addSubmenuItem("Avis", makeAvisContextMenu(targetPlayer));
            ContextMenuTrigger.contextMenu.open();

            if (ContextMenuTrigger.customMenu != null)
                ContextMenuTrigger.contextMenu.merge(CustomContextMenuLoader.load(ContextMenuTrigger.customMenu, targetPlayer));

            if (new File(".hbtweaks_debug").isFile())
                ContextMenuTrigger.contextMenu.addSubmenuItem("Debug", makeDebugContextMenu(targetPlayer));

            // TODO Open context menu
        }
    }

    private static void render(GuiGraphics graphics, DeltaTracker tickDelta) {
        if (contextMenu != null) {
            if (Minecraft.getInstance().mouseHandler.isMouseGrabbed()) {
                contextMenu.close();
                contextMenu = null;
            } else {
                contextMenu.render(graphics, tickDelta);
            }
        }
    }

    @Override
    public void afterMouseClick(Screen screen, double x, double y, int button) {
        if (ContextMenuTrigger.contextMenu != null) {
            if (ContextMenuTrigger.contextMenu.mouseClicked(x, y, button)) {
                ContextMenuTrigger.contextMenu.close();
                ContextMenuTrigger.contextMenu = null;
            }
        }
    }
}
