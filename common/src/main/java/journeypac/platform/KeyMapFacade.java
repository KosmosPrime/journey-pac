package journeypac.platform;

import net.minecraft.client.KeyMapping;

public interface KeyMapFacade
{
	public KeyMapping createGui(String category, String description, int keyCode);
}
