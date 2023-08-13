package journeypac.platform;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;

public final class FabricKeyMapFacade implements KeyMapFacade
{
	private final List<KeyMapping> guiMappings = new ArrayList<>();
	
	public KeyMapping createGui(String category, String description, int keyCode)
	{
		KeyMapping mapping = new KeyMapping(description, keyCode, category);
		guiMappings.add(mapping);
		return mapping;
	}
	
	protected void onInit()
	{
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
		{
			// manually check keybinds because the fullscreen map doesn't pass events
			ScreenKeyboardEvents.afterKeyPress(screen).register(this::onKeyPressed);
			ScreenKeyboardEvents.afterKeyRelease(screen).register(this::onKeyReleased);
			ScreenMouseEvents.afterMouseClick(screen).register(this::onMousePressed);
			ScreenMouseEvents.afterMouseRelease(screen).register(this::onMouseReleased);
		});
	}
	
	protected void onRegister()
	{
		guiMappings.forEach(KeyBindingHelper::registerKeyBinding);
	}
	
	// for handling fabric events
	private final void onKeyPressed(Screen screen, int keyCode, int scanCode, int mods)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matches(keyCode, scanCode)) curr.setDown(true);
		}
	}
	
	private final void onKeyReleased(Screen screen, int keyCode, int scanCode, int mods)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matches(keyCode, scanCode)) curr.setDown(false);
		}
	}
	
	private final void onMousePressed(Screen screen, double mouseX, double mouseY, int button)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matchesMouse(button)) curr.setDown(true);
		}
	}
	
	private final void onMouseReleased(Screen screen, double mouseX, double mouseY, int button)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matchesMouse(button)) curr.setDown(false);
		}
	}
}
