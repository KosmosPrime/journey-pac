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
	public ForgeMain(FMLJavaModLoadingContext context)
	{
		JourneyPAC.create(ForgeConfig.CONFIG, new ForgeKeyMapFacade(), new ForgeEventFacade());
		context.registerConfig(ModConfig.Type.CLIENT, ForgeConfig.SPEC);
		ModConfigEvent.Reloading.getBus(context.getModBusGroup()).addListener(this::onConfigReload);
	}
	
	private void onConfigReload(ModConfigEvent.Reloading event)
	{
		if (event.getConfig().getSpec() == ForgeConfig.SPEC) ForgeConfig.CONFIG.fireConfigReload();
	}
}
