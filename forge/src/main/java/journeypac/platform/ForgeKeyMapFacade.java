package journeypac.platform;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ForgeKeyMapFacade implements KeyMapFacade
{
	public ForgeKeyMapFacade(FMLJavaModLoadingContext context)
	{
		RegisterKeyMappingsEvent.getBus(context.getModBusGroup()).addListener(this::onRegister);
		
		// manually check keybinds because the fullscreen map doesn't pass events
		ScreenEvent.KeyPressed.Post.BUS.addListener(this::onKeyPressed);
		ScreenEvent.KeyReleased.Post.BUS.addListener(this::onKeyReleased);
		ScreenEvent.MouseButtonPressed.Post.BUS.addListener(this::onMousePressed);
		ScreenEvent.MouseButtonReleased.Post.BUS.addListener(this::onMouseReleased);
	}
	
	private final List<KeyMapping> guiMappings = new ArrayList<>();
	
	public KeyMapping createGui(String category, String description, int keyCode)
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
