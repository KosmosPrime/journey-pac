package journeypac;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(JourneyPAC.MODID)
public class JourneyPAC
{
	public static final String MODID = "journeypac";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public JourneyPAC()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JPACConfig.SPEC);
	}
}
