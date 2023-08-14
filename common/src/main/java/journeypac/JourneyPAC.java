package journeypac;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import journeypac.platform.ConfigFacade;
import journeypac.platform.EventFacade;
import journeypac.platform.KeyMapFacade;

public final class JourneyPAC
{
	public static final String MODID = "journeypac";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	private static volatile JourneyPAC instance;
	
	public static synchronized JourneyPAC create(ConfigFacade config, KeyMapFacade keyMap, EventFacade events)
	{
		if (instance != null) throw new IllegalStateException("mod already constructed");
		JourneyPAC inst = new JourneyPAC(config, keyMap, events);
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
	private final KeyMappings keyMap;
	private final EventFacade events;
	
	private JourneyPAC(ConfigFacade config, KeyMapFacade keyMap, EventFacade events)
	{
		if (config == null) throw new NullPointerException("config");
		if (keyMap == null) throw new NullPointerException("keyMap");
		if (events == null) throw new NullPointerException("events");
		this.config = config;
		this.keyMap = new KeyMappings(keyMap);
		this.events = events;
	}
	
	public ConfigFacade getConfig()
	{
		return config;
	}
	
	public KeyMappings getKeyMappings()
	{
		return keyMap;
	}
	
	public EventFacade getEvents()
	{
		return events;
	}
}
