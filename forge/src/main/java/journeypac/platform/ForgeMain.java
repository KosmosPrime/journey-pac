package journeypac.platform;

import journeypac.JourneyPAC;
import journeypac.platform.ForgeKeyMapFacade;
import journeypac.platform.JPACConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JourneyPAC.MODID)
public final class ForgeMain
{
	public ForgeMain()
	{
		JourneyPAC.create(JPACConfig.CONFIG, new ForgeKeyMapFacade());
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JPACConfig.SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
	}
	
	private void onConfigReload(ModConfigEvent.Reloading event)
	{
		if (event.getConfig().getSpec() == JPACConfig.SPEC) JPACConfig.CONFIG.fireConfigReload();
	}
}
