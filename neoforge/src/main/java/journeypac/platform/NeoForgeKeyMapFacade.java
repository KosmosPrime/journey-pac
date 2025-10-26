package journeypac.platform;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;

public final class NeoForgeKeyMapFacade implements KeyMapFacade
{
	public NeoForgeKeyMapFacade()
	{
		ModContainer container = ModLoadingContext.get().getActiveContainer();
		container.getEventBus().addListener(this::onRegister);
		
		// manually check keybinds because the fullscreen map doesn't pass events
		NeoForge.EVENT_BUS.addListener(this::onKeyPressed);
		NeoForge.EVENT_BUS.addListener(this::onKeyReleased);
		NeoForge.EVENT_BUS.addListener(this::onMousePressed);
		NeoForge.EVENT_BUS.addListener(this::onMouseReleased);
	}
	
	private final List<KeyMapping> guiMappings = new ArrayList<>();
	
	public KeyMapping createGui(KeyMapping.Category category, String description, int keyCode)
	{
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
		KeyMapping mapping = new KeyMapping(description, KeyConflictContext.GUI, KeyModifier.NONE, key, category);
		guiMappings.add(mapping);
		return mapping;
	}
	
	private void onRegister(RegisterKeyMappingsEvent event)
	{
		guiMappings.forEach(event::register);
	}
	
	// for the forge event bus
	private final void onKeyPressed(ScreenEvent.KeyPressed.Post event)
	{
		onInput(InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode()), true);
	}
	
	private final void onKeyReleased(ScreenEvent.KeyReleased.Post event)
	{
		onInput(InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode()), false);
	}
	
	private final void onMousePressed(ScreenEvent.MouseButtonPressed.Post event)
	{
		onInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()), true);
	}
	
	private final void onMouseReleased(ScreenEvent.MouseButtonReleased.Post event)
	{
		onInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()), false);
	}
	
	private final void onInput(InputConstants.Key key, boolean pressed)
	{
		for (KeyMapping curr : guiMappings)
		{
			if (curr.isActiveAndMatches(key)) curr.setDown(pressed);
		}
	}
}
