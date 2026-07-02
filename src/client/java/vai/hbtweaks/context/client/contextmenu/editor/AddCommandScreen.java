package vai.hbtweaks.context.client.contextmenu.editor;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddCommandScreen extends EditorScreen {
    private static final String[][] TAGS = {
            {"%mcname%", "hbtweaks.context.editor.tag.mcname"},
            {"%rpname%", "hbtweaks.context.editor.tag.rpname"},
            {"%blockpos%", "hbtweaks.context.editor.tag.blockpos"},
            {"%eyepos%", "hbtweaks.context.editor.tag.eyepos"},
            {"%uuid%", "hbtweaks.context.editor.tag.uuid"},
            {"%mymcname%", "hbtweaks.context.editor.tag.mymcname"},
            {"%myrpname%", "hbtweaks.context.editor.tag.myrpname"},
            {"%myblockpos%", "hbtweaks.context.editor.tag.myblockpos"},
            {"%myeyepos%", "hbtweaks.context.editor.tag.myeyepos"},
            {"%myuuid%", "hbtweaks.context.editor.tag.myuuid"},
    };

    private final MenuLocation location;
    private SpruceTextFieldWidget nameField;
    private SpruceTextFieldWidget commandField;

    public AddCommandScreen(Screen parent, MenuLocation location) {
        super(parent, Component.translatable("hbtweaks.context.editor.add_command"), 460, 160);
        this.location = location;
    }

    @Override
    protected void build(SpruceContainerWidget panel) {
        int pad = EditorStyle.PADDING;
        int leftW = 200;
        int y = pad + 16;

        Component nameLabel = Component.translatable("hbtweaks.context.editor.name");
        Component commandLabel = Component.translatable("hbtweaks.context.editor.command");

        panel.addChild(new SpruceLabelWidget(Position.of(panel, pad, y), nameLabel, leftW));
        y += 11;
        this.nameField = new SpruceTextFieldWidget(Position.of(panel, pad, y),
                leftW, EditorStyle.FIELD_H, nameLabel);
        panel.addChild(this.nameField);
        y += EditorStyle.FIELD_H + EditorStyle.ROW_GAP;

        panel.addChild(new SpruceLabelWidget(Position.of(panel, pad, y), commandLabel, leftW));
        y += 11;
        this.commandField = new SpruceTextFieldWidget(Position.of(panel, pad, y),
                leftW, EditorStyle.FIELD_H, commandLabel);
        this.commandField.setText("/");
        this.commandField.setTextPredicate(s -> s.startsWith("/"));
        panel.addChild(this.commandField);
        y += EditorStyle.FIELD_H + EditorStyle.ROW_GAP + 2;

        int btnW = (leftW - EditorStyle.ROW_GAP) / 2;
        panel.addChild(new EditorButton(Position.of(panel, pad, y), btnW, EditorStyle.BTN_H,
                Component.translatable("hbtweaks.context.editor.button.cancel"), this::onClose));
        panel.addChild(new EditorButton(Position.of(panel, pad + btnW + EditorStyle.ROW_GAP, y),
                btnW, EditorStyle.BTN_H, Component.translatable("hbtweaks.context.editor.button.add"), this::submit));

        int csX = pad + leftW + pad;
        int csW = this.panelW - csX - pad;
        int csY = pad + 16;
        SpruceLabelWidget csTitle = new SpruceLabelWidget(Position.of(panel, csX, csY),
                Component.translatable("hbtweaks.context.editor.tags"), csW);
        csTitle.setColor(EditorStyle.TEXT);
        panel.addChild(csTitle);
        csY += 12;
        for (String[] tag : TAGS) {
            Component line = Component.literal(tag[0]).withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(" ").append(Component.translatable(tag[1])).withStyle(ChatFormatting.GRAY));
            SpruceLabelWidget lw = new SpruceLabelWidget(Position.of(panel, csX, csY), line, csW);
            panel.addChild(lw);
            csY += 11;
        }
    }

    private void submit() {
        String name = this.nameField.getText().trim();
        String command = this.commandField.getText().trim();
        if (command.startsWith("/"))
            command = command.substring(1).trim();
        if (name.isEmpty() || command.isEmpty()) return;
        this.location.addCommand(name, command);
        this.done();
    }
}
