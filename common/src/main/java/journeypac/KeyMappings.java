package journeypac;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import journeypac.platform.KeyMapFacade;

public class KeyMappings
{
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(JourneyPAC.MODID, "category"));
	
	private static KeyMapping createGui(KeyMapFacade keyMap, KeyMapping.Category category, String description, int keyCode)
	{
		description = Util.makeDescriptionId("key", Identifier.fromNamespaceAndPath(JourneyPAC.MODID, description));
		return keyMap.createGui(category, description, keyCode);
	}
	
	private final KeyMapping claimMode;
	private final KeyMapping forceloadMode;
	
	public KeyMappings(KeyMapFacade keyMap)
	{
		claimMode = createGui(keyMap, KEY_CATEGORY, "claim_mode", GLFW.GLFW_KEY_U);
		forceloadMode = createGui(keyMap, KEY_CATEGORY, "forceload_mode", GLFW.GLFW_KEY_I);
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
