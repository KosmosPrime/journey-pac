package journeypac.platform;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import journeypac.JourneyPAC;

@Mod(JourneyPAC.MODID)
public final class ForgeMain
{
	public ForgeMain()
	{
		JourneyPAC.create(ForgeConfig.CONFIG, new ForgeKeyMapFacade(), new ForgeEventFacade());
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfig.SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
	}
	
	private void onConfigReload(ModConfigEvent.Reloading event)
	{
		if (event.getConfig().getSpec() == ForgeConfig.SPEC) ForgeConfig.CONFIG.fireConfigReload();
	}
}
