package journeypac.platform;

import net.minecraft.client.KeyMapping;

public interface KeyMapFacade
{
	public KeyMapping createGui(KeyMapping.Category category, String description, int keyCode);
}
