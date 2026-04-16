package vai.hbtweaks.context.client.contextmenu;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import vai.hbtweaks.context.HBTweaksContext;

import java.util.ArrayList;

public class ContextMenu {

    private static final int ITEM_HEIGHT = 16;
    private static final int PADDING_X = 6;
    private static final int MIN_WIDTH = 20;
    private static final int ARROW_RIGHT_PAD = 8;

    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_TEXT_HOVER = 0xFFFFFFFF;
    private static final int COLOR_BG = 0xE0101010;
    private static final int COLOR_HOVER = 0xFF2255AA;
    private static final int COLOR_BORDER = 0xFF3A3A3A;

    private int x;
    private int y;

    private final List<MenuItem> items = new ArrayList<>();
    private boolean visible = false;
    private int width = MIN_WIDTH;
    private ContextMenu openSubmenu = null;

    public ContextMenu(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ContextMenu addActionItem(String label, Runnable action) {
        return this.addActionItem(Component.literal(label), action);
    }

    public ContextMenu addActionItem(Component label, Runnable action) {
        this.items.add(new ActionItem(label, action));
        recalcWidth();
        return this;
    }

    public ContextMenu addCommandItem(String label, String commandTemplate) {
        return this.addCommandItem(Component.literal(label), commandTemplate);
    }

    public ContextMenu addCommandItem(Component label, String commandTemplate) {
        this.items.add(new CommandItem(label, commandTemplate));
        recalcWidth();
        return this;
    }

    public ContextMenu addInfoItem(String label) {
        return this.addInfoItem(Component.literal(label));
    }

    public ContextMenu addInfoItem(Component label) {
        this.items.add(new InfoItem(label));
        recalcWidth();
        return this;
    }

    public ContextMenu addSubmenuItem(String label, ContextMenu submenu) {
        return this.addSubmenuItem(Component.literal(label), submenu);
    }

    public ContextMenu addSubmenuItem(Component label, ContextMenu submenu) {
        this.items.add(new SubmenuItem(label, submenu));
        recalcWidth();
        return this;
    }

    public void open() {
        this.visible = true;
    }

    public void close() {
        this.visible = false;
        if (this.openSubmenu != null) {
            this.openSubmenu.close();
            this.openSubmenu = null;
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void render(GuiGraphics graphics, DeltaTracker tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        render(graphics,
                (int) mc.mouseHandler.getScaledXPos(mc.getWindow()),
                (int) mc.mouseHandler.getScaledYPos(mc.getWindow()),
                tickDelta);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, DeltaTracker tickDelta) {
        if (!this.visible) return;

        Minecraft mc = Minecraft.getInstance();

        int totalHeight = this.items.size() * ContextMenu.ITEM_HEIGHT;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        this.x = Math.min(this.x, screenW - this.width - 1);
        this.y = Math.min(this.y, screenH - totalHeight - 1);

        graphics.fill(this.x - 1,
                this.y - 1,
                this.x + this.width + 1,
                this.y + totalHeight + 1,
                ContextMenu.COLOR_BORDER);
        graphics.fill(this.x,
                this.y,
                this.x + this.width,
                this.y + totalHeight,
                ContextMenu.COLOR_BG);

        ContextMenu nextSubmenu = null;
        int nextSubmenuIndex = -1;

        for (int i = 0; i < this.items.size(); i++) {
            MenuItem item = this.items.get(i);
            int itemY = this.y + i * ContextMenu.ITEM_HEIGHT;

            boolean hovered = isInsideRow(mouseX, mouseY, itemY);
            if (item instanceof InfoItem) {
                hovered = false;
            }

            if (hovered) {
                graphics.fill(this.x, itemY, this.x + this.width, itemY + ContextMenu.ITEM_HEIGHT, ContextMenu.COLOR_HOVER);
            }

            int textColor = hovered ? ContextMenu.COLOR_TEXT_HOVER : ContextMenu.COLOR_TEXT;
            graphics.drawString(mc.font, item.getLabel(), this.x + ContextMenu.PADDING_X, itemY + 4, textColor, false);

            if (item instanceof SubmenuItem si) {
                graphics.drawString(mc.font, ">", this.x + this.width - ContextMenu.ARROW_RIGHT_PAD, itemY + 4, textColor, false);
                if (hovered) {
                    nextSubmenu = si.submenu;
                    nextSubmenuIndex = i;
                }
            }
        }

        if (nextSubmenu != null) {
            if (this.openSubmenu != nextSubmenu) {
                if (this.openSubmenu != null)
                    this.openSubmenu.close();
                this.openSubmenu = nextSubmenu;
                this.openSubmenu.x = this.x + this.width;
                this.openSubmenu.y = this.y + nextSubmenuIndex * ContextMenu.ITEM_HEIGHT;
                this.openSubmenu.open();
            }
        } else {
            if (this.openSubmenu != null && !this.openSubmenu.containsMouseRecursive(mouseX, mouseY)) {
                this.openSubmenu.close();
                this.openSubmenu = null;
            }
        }

        if (this.openSubmenu != null) {
            this.openSubmenu.render(graphics, mouseX, mouseY, tickDelta);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        }

        if (this.openSubmenu != null && this.openSubmenu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        for (int i = 0; i < this.items.size(); i++) {
            int itemY = this.y + i * ContextMenu.ITEM_HEIGHT;
            if (isInsideRow(mx, my, itemY)) {
                MenuItem item = this.items.get(i);
                if (!(item instanceof SubmenuItem) && !(item instanceof InfoItem)) {
                    item.onClick();
                    close();
                } else {
                    return false;
                }
                return true;
            }
        }

        if (!containsMouseRecursive(mx, my)) {
            close();
        }
        return false;
    }

    public boolean containsMouse(int mx, int my) {
        return mx >= this.x
                && mx < this.x + this.width
                && my >= this.y
                && my < this.y + this.items.size() * ContextMenu.ITEM_HEIGHT;
    }

    public boolean containsMouseRecursive(int mx, int my) {
        if (this.containsMouse(mx, my))
            return true;
        for (MenuItem m : this.items) {
            if (m instanceof SubmenuItem) {
                if (((SubmenuItem)m).submenu.containsMouseRecursive(mx, my))
                    return true;
            }
        }
        return false;
    }

    private boolean isInsideRow(int mx, int my, int rowY) {
        return mx >= this.x
                && mx < this.x + this.width
                && my >= rowY
                && my < rowY + ContextMenu.ITEM_HEIGHT;
    }

    private void recalcWidth() {
        Minecraft mc = Minecraft.getInstance();
        int max = 0;
        for (MenuItem item : this.items) {
            int lw = mc.font.width(item.getLabel());
            if (item instanceof SubmenuItem)
                lw += ContextMenu.ARROW_RIGHT_PAD + 4;
            if (lw > max)
                max = lw;
        }
        this.width = Math.max(ContextMenu.MIN_WIDTH, max + ContextMenu.PADDING_X * 2);
    }

    private interface MenuItem {
        Component getLabel();
        void onClick();
    }

    private static final class ActionItem implements MenuItem {
        private final Component label;
        private final Runnable action;

        ActionItem(Component label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        @Override
        public Component getLabel() {
            return this.label;
        }

        @Override
        public void onClick() {
            this.action.run();
        }
    }

    private static final class CommandItem implements MenuItem {

        private final Component label;
        private final String command;

        CommandItem(Component label, String command) {
            this.label = label;
            this.command = command;
        }

        @Override
        public Component getLabel() {
            return this.label;
        }

        @Override
        public void onClick() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
                return;
            HBTweaksContext.LOGGER.info("COMMAND RUN : " + this.command);
            mc.player.connection.sendCommand(this.command);
        }
    }

    private static final class SubmenuItem implements MenuItem {
        private final Component label;
        final ContextMenu submenu;

        SubmenuItem(Component label, ContextMenu submenu) {
            this.label = label;
            this.submenu = submenu;
        }

        @Override
        public Component getLabel() {
            return this.label;
        }

        @Override
        public void onClick() {
            // Nothing
        }
    }

    private static final class InfoItem implements MenuItem {
        private final Component label;

        InfoItem(Component label) {
            this.label = label;
        }

        @Override
        public Component getLabel() {
            return this.label;
        }

        @Override public void onClick() {
            // Nothing
        }
    }
}