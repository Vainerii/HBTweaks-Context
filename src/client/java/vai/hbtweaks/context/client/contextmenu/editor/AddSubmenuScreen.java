package vai.hbtweaks.context.client.contextmenu.editor;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddSubmenuScreen extends EditorScreen {
    private final MenuLocation location;
    private SpruceTextFieldWidget nameField;

    public AddSubmenuScreen(Screen parent, MenuLocation location) {
        super(parent, Component.translatable("hbtweaks.context.editor.add_submenu"), 220, 92);
        this.location = location;
    }

    @Override
    protected void build(SpruceContainerWidget panel) {
        int pad = EditorStyle.PADDING;
        int innerW = this.panelW - pad * 2;
        int y = pad + 14;

        Component nameLabel = Component.translatable("hbtweaks.context.editor.name");
        panel.addChild(new SpruceLabelWidget(Position.of(panel, pad, y), nameLabel, innerW));
        y += 11;

        this.nameField = new SpruceTextFieldWidget(Position.of(panel, pad, y),
                innerW, EditorStyle.FIELD_H, nameLabel);
        panel.addChild(this.nameField);
        y += EditorStyle.FIELD_H + EditorStyle.ROW_GAP + 2;

        int btnW = (innerW - EditorStyle.ROW_GAP) / 2;
        panel.addChild(new EditorButton(Position.of(panel, pad, y), btnW, EditorStyle.BTN_H,
                Component.translatable("hbtweaks.context.editor.button.cancel"), this::onClose));
        panel.addChild(new EditorButton(Position.of(panel, pad + btnW + EditorStyle.ROW_GAP, y),
                btnW, EditorStyle.BTN_H, Component.translatable("hbtweaks.context.editor.button.add"), this::submit));
    }

    private void submit() {
        String name = this.nameField.getText().trim();
        if (name.isEmpty()) return;
        this.location.addSubmenu(name);
        this.done();
    }
}
