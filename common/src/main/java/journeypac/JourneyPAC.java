package journeypac;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public final class JourneyPAC
{
	public static final String MODID = "journeypac";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	private static volatile JourneyPAC instance;
	
	public static synchronized JourneyPAC create()
	{
		if (instance != null) throw new IllegalStateException("mod already constructed");
		JourneyPAC inst = new JourneyPAC();
		instance = inst;
		return inst;
	}
	
	public static JourneyPAC getInstance()
	{
		JourneyPAC inst = instance;
		if (inst == null) throw new IllegalStateException("mod not constructed");
		return inst;
	}
	
	private JourneyPAC()
	{
	}
}
