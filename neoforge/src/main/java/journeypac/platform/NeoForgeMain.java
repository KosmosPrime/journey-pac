package journeypac.platform;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

import journeypac.JourneyPAC;

@Mod(JourneyPAC.MODID)
public final class NeoForgeMain
{
	public NeoForgeMain()
	{
		JourneyPAC.create(NeoForgeConfig.CONFIG, new NeoForgeKeyMapFacade(), new NeoForgeEventFacade());
		ModContainer container = ModLoadingContext.get().getActiveContainer();
		container.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.SPEC);
		container.getEventBus().addListener(this::onConfigReload);
	}
	
	private void onConfigReload(ModConfigEvent.Reloading event)
	{
		if (event.getConfig().getSpec() == NeoForgeConfig.SPEC) NeoForgeConfig.CONFIG.fireConfigReload();
	}
}
