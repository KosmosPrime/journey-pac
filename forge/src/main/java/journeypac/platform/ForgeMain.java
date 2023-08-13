package journeypac.platform;

import journeypac.JourneyPAC;
import journeypac.JPACConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(JourneyPAC.MODID)
public final class ForgeMain
{
	public ForgeMain()
	{
		JourneyPAC.create();
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JPACConfig.SPEC);
	}
}
