package journeypac.platform;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public final class FabricKeyMapFacade implements KeyMapFacade
{
	private final List<KeyMapping> guiMappings = new ArrayList<>();
	
	public KeyMapping createGui(KeyMapping.Category category, String description, int keyCode)
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
	private final void onKeyPressed(Screen screen, KeyEvent event)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matches(event)) curr.setDown(true);
		}
	}
	
	private final void onKeyReleased(Screen screen, KeyEvent event)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matches(event)) curr.setDown(false);
		}
	}
	
	private final boolean onMousePressed(Screen screen, MouseButtonEvent event, boolean consumed)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matchesMouse(event))
			{
				curr.setDown(true);
				return true;
			}
		}
		return false;
	}
	
	private final boolean onMouseReleased(Screen screen, MouseButtonEvent event, boolean consumed)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.matchesMouse(event))
			{
				curr.setDown(false);
				return true;
			}
		}
		return false;
	}
}
