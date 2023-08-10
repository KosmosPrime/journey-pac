package journeypac;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = JourneyPAC.MODID, bus = Bus.MOD)
public class KeyMappings
{
	private static final List<KeyMapping> keyMappings = new ArrayList<>();
	
	private static KeyMapping create(String category, String description, int keyCode, KeyModifier modifier)
	{
		description = Util.makeDescriptionId("key", new ResourceLocation(JourneyPAC.MODID, description));
		category = Util.makeDescriptionId("key", new ResourceLocation(JourneyPAC.MODID, category));
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
		KeyMapping mapping = new KeyMapping(description, KeyConflictContext.GUI, modifier, key, category);
		keyMappings.add(mapping);
		return mapping;
	}
	
	public static final KeyMapping CLAIM_MODE = create("category", "claim_mode", GLFW.GLFW_KEY_TAB, KeyModifier.NONE);
	public static final KeyMapping FORCELOAD_MODE = create("category", "forceload_mode", GLFW.GLFW_KEY_Q, KeyModifier.NONE);
	
	@SubscribeEvent
	public static final void register(RegisterKeyMappingsEvent event)
	{
		event.register(CLAIM_MODE);
		event.register(FORCELOAD_MODE);
		
		// manually check keybinds because the fullscreen map doesn't pass events
		MinecraftForge.EVENT_BUS.addListener(KeyMappings::onKeyPressed);
		MinecraftForge.EVENT_BUS.addListener(KeyMappings::onKeyReleased);
		MinecraftForge.EVENT_BUS.addListener(KeyMappings::onMousePressed);
		MinecraftForge.EVENT_BUS.addListener(KeyMappings::onMouseReleased);
	}
	
	// for the forge event bus
	public static final void onKeyPressed(ScreenEvent.KeyPressed.Post event)
	{
		onInput(InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode()), true);
	}
	
	public static final void onKeyReleased(ScreenEvent.KeyReleased.Post event)
	{
		onInput(InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode()), false);
	}
	
	public static final void onMousePressed(ScreenEvent.MouseButtonPressed.Post event)
	{
		onInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()), true);
	}
	
	public static final void onMouseReleased(ScreenEvent.MouseButtonReleased.Post event)
	{
		onInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()), false);
	}
	
	private static final void onInput(InputConstants.Key key, boolean pressed)
	{
		for (KeyMapping curr : keyMappings)
		{
			if (curr.isActiveAndMatches(key)) curr.setDown(pressed);
		}
	}
	
	public static enum ClaimMode
	{
		NONE, CLAIM, FORCELOAD;
	}
	
	public static ClaimMode getClaimMode()
	{
		if (FORCELOAD_MODE.isDown()) return ClaimMode.FORCELOAD;
		else if (CLAIM_MODE.isDown()) return ClaimMode.CLAIM;
		else return ClaimMode.NONE;
	}
}
