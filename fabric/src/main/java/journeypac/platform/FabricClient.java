package journeypac.platform;

import journeypac.JourneyPAC;
import journeypac.platform.FabricEventFacade;
import journeypac.platform.FabricKeyMapFacade;
import journeypac.platform.JPACConfig;
import net.fabricmc.api.ClientModInitializer;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.minecraftforge.fml.config.ModConfig;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;

public final class FabricClient implements ClientModInitializer
{
	private FabricKeyMapFacade keyMap;
	
	public FabricClient()
	{
		keyMap = new FabricKeyMapFacade();
		JourneyPAC.create(JPACConfig.CONFIG, keyMap, new FabricEventFacade());
	}
	
	public void onInitializeClient()
	{
		ForgeConfigRegistry.INSTANCE.register(JourneyPAC.MODID, ModConfig.Type.CLIENT, JPACConfig.SPEC);
		ModConfigEvents.reloading(JourneyPAC.MODID).register(config ->
		{
			if (config.getSpec() == JPACConfig.SPEC) JPACConfig.CONFIG.fireConfigReload();
		});
		
		keyMap.onInit();
		keyMap.onRegister();
	}
}
