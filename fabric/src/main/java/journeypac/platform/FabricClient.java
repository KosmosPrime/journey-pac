package journeypac.platform;

import journeypac.JourneyPAC;
import journeypac.platform.JPACConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.api.fml.event.config.ModConfigEvents;

public final class FabricClient implements ClientModInitializer
{
	public FabricClient()
	{
		JourneyPAC.create(JPACConfig.CONFIG);
	}
	
	public void onInitializeClient()
	{
		ModLoadingContext.registerConfig(JourneyPAC.MODID, ModConfig.Type.CLIENT, JPACConfig.SPEC);
		ModConfigEvents.reloading(JourneyPAC.MODID).register(config ->
		{
			if (config.getSpec() == JPACConfig.SPEC) JPACConfig.CONFIG.fireConfigReload();
		});
	}
}
