package vai.hbtweaks.context.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import vai.hbtweaks.context.HBTweaksContext;
import vai.hbtweaks.context.client.listeners.ContextMenuTrigger;
import vai.hbtweaks.context.client.listeners.SendMessageTrigger;
import vai.mousepointerapi.events.MouseTrackerEntityClickUpCallback;

public class HBTweaksContextClient implements ClientModInitializer {

	private static final ContextMenuTrigger cmt = new ContextMenuTrigger();
	private static final SendMessageTrigger smt = new SendMessageTrigger();

	@Override
	public void onInitializeClient() {
		MouseTrackerEntityClickUpCallback.EVENT.register(cmt);
		MouseTrackerEntityClickUpCallback.EVENT.register(smt);
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof ChatScreen) {
				ScreenMouseEvents.afterMouseClick(screen).register(cmt);
			}
		});

	}
}