package journeypac.platform;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import journeypac.JourneyPAC;

public final class FabricClient implements ClientModInitializer
{
	private FabricConfig config;
	private FabricKeyMapFacade keyMap;
	
	public FabricClient()
	{
		config = new FabricConfig(FabricLoader.getInstance().getConfigDir());
		keyMap = new FabricKeyMapFacade();
		JourneyPAC.create(config, keyMap, new FabricEventFacade());
	}
	
	public void onInitializeClient()
	{
		config.load(true);
		keyMap.onInit();
		keyMap.onRegister();
	}
}
