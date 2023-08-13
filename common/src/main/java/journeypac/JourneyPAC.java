package journeypac;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import journeypac.platform.ConfigFacade;

public final class JourneyPAC
{
	public static final String MODID = "journeypac";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	private static volatile JourneyPAC instance;
	
	public static synchronized JourneyPAC create(ConfigFacade config)
	{
		if (instance != null) throw new IllegalStateException("mod already constructed");
		JourneyPAC inst = new JourneyPAC(config);
		instance = inst;
		return inst;
	}
	
	public static JourneyPAC getInstance()
	{
		JourneyPAC inst = instance;
		if (inst == null) throw new IllegalStateException("mod not constructed");
		return inst;
	}
	
	private final ConfigFacade config;
	
	private JourneyPAC(ConfigFacade config)
	{
		if (config == null) throw new NullPointerException("config");
		this.config = config;
	}
	
	public ConfigFacade getConfig()
	{
		return config;
	}
}
