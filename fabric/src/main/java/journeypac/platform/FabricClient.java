package journeypac.platform;

import journeypac.JourneyPAC;
import journeypac.platform.FabricKeyMapFacade;
import journeypac.platform.JPACConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.api.fml.event.config.ModConfigEvents;

public final class FabricClient implements ClientModInitializer
{
	private FabricKeyMapFacade keyMap;
	
	public FabricClient()
	{
		keyMap = new FabricKeyMapFacade();
		JourneyPAC.create(JPACConfig.CONFIG, keyMap);
	}
	
	public void onInitializeClient()
	{
		ModLoadingContext.registerConfig(JourneyPAC.MODID, ModConfig.Type.CLIENT, JPACConfig.SPEC);
		ModConfigEvents.reloading(JourneyPAC.MODID).register(config ->
		{
			if (config.getSpec() == JPACConfig.SPEC) JPACConfig.CONFIG.fireConfigReload();
		});
		
		keyMap.onInit();
		keyMap.onRegister();
	}
}
