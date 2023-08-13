package journeypac;

import org.lwjgl.glfw.GLFW;

import journeypac.platform.KeyMapFacade;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public class KeyMappings
{
	private static KeyMapping createGui(KeyMapFacade keyMap, String category, String description, int keyCode)
	{
		category = Util.makeDescriptionId("key", new ResourceLocation(JourneyPAC.MODID, category));
		description = Util.makeDescriptionId("key", new ResourceLocation(JourneyPAC.MODID, description));
		return keyMap.createGui(category, description, keyCode);
	}
	
	private final KeyMapping claimMode;
	private final KeyMapping forceloadMode;
	
	public KeyMappings(KeyMapFacade keyMap)
	{
		claimMode = createGui(keyMap, "category", "claim_mode", GLFW.GLFW_KEY_TAB);
		forceloadMode = createGui(keyMap, "category", "forceload_mode", GLFW.GLFW_KEY_Q);
	}
	
	public static enum ClaimMode
	{
		NONE, CLAIM, FORCELOAD;
	}
	
	public ClaimMode getClaimMode()
	{
		if (forceloadMode.isDown()) return ClaimMode.FORCELOAD;
		else if (claimMode.isDown()) return ClaimMode.CLAIM;
		else return ClaimMode.NONE;
	}
}
