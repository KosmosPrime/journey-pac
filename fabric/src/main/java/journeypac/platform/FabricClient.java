package journeypac.platform;

import journeypac.JourneyPAC;
import net.fabricmc.api.ClientModInitializer;

public final class FabricClient implements ClientModInitializer
{
	public FabricClient()
	{
		JourneyPAC.create();
	}
	
	public void onInitializeClient()
	{
	}
}
