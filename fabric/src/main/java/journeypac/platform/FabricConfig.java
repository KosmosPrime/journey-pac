package journeypac.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;

import journeypac.JourneyPAC;

public class FabricConfig implements ConfigFacade
{
	private static final ConfigEntry[] CONFIGS;
	private static final HashMap<String, ConfigEntry> CONFIG_MAP;
	static
	{
		CONFIGS = new ConfigEntry[]
		{
				new ConfigEntry.BooleanConfig(ConfigFacade.SHOW_CLAIMS,
						c -> c.showClaims, (c, v) -> c.showClaims = v),
				new ConfigEntry.BooleanConfig(ConfigFacade.SHOW_FORCELOADS,
						c -> c.showForceloads, (c, v) -> c.showForceloads = v),
				new ConfigEntry.BooleanConfig(ConfigFacade.SHOW_CLAIMANT,
						c -> c.showClaimant, (c, v) -> c.showClaimant = v),
				new ConfigEntry.DoubleConfig(ConfigFacade.CLAIM_OPACITY,
						c -> c.claimOpacity, (c, v) -> c.claimOpacity = v),
				new ConfigEntry.DoubleConfig(ConfigFacade.FORCELOAD_OPACITY,
						c -> c.forceloadOpacity, (c, v) -> c.forceloadOpacity = v),
				new ConfigEntry.DoubleConfig(ConfigFacade.FORCELOAD_STROKE,
						c -> c.forceloadStroke, (c, v) -> c.forceloadStroke = v),
				new ConfigEntry.DoubleConfig(ConfigFacade.VALID_AREA_OPACITY,
						c -> c.validAreaOpacity, (c, v) -> c.validAreaOpacity = v),
		};
		
		CONFIG_MAP = new HashMap<>();
		for (int i = 0; i < CONFIGS.length; i++)
		{
			CONFIGS[i].setIndex(i);
			CONFIG_MAP.put(CONFIGS[i].getInfo().getName(), CONFIGS[i]);
		}
	}
	
	private final Path path;
	private boolean showClaims;
	private boolean showForceloads;
	private boolean showClaimant;
	private double claimOpacity;
	private double forceloadOpacity;
	private double forceloadStroke;
	private double validAreaOpacity;
	
	public FabricConfig(Path folder)
	{
		path = folder.resolve(JourneyPAC.MODID + "-client.toml");
		for (ConfigEntry cfg : CONFIGS)
		{
			cfg.reset(this);
		}
	}
	
	public void load(boolean initial)
	{
		boolean[] sync = new boolean[1 + CONFIGS.length];
		sync[0] = true; // default
		try (Stream<String> lines = Files.lines(path))
		{
			lines.map(String::trim).filter(l -> !l.isEmpty() && l.charAt(0) != '#').forEach(line ->
			{
				int idx = line.indexOf('=');
				if (idx < 0)
				{
					sync[0] = false; // restore a valid config
					JourneyPAC.LOGGER.warn("Malformed configuration file");
					return;
				}
				String key = line.substring(0, idx).trim();
				ConfigEntry cfg = CONFIG_MAP.get(key);
				if (cfg == null)
				{
					sync[0] = false; // restore a valid config
					JourneyPAC.LOGGER.warn("No such config element: " + key);
					return;
				}
				if (initial || cfg.getInfo().isMutable())
				{
					try
					{
						sync[0] &= !sync[1 + cfg.getIndex()]; // save if any duplicates
						sync[1 + cfg.getIndex()] = true; // remember that we've seen this config
						sync[0] &= cfg.load(this, line.substring(idx + 1).trim()); // save if modified
					}
					catch (Exception e)
					{
						JourneyPAC.LOGGER.warn("Could not load config " + cfg.getInfo().getName()
								+ System.lineSeparator() + e.toString());
						cfg.reset(this);
						sync[0] = false; // save on error
					}
				}
			});
			// ensure all keys are present
			for (int i = 1; i <= CONFIGS.length; i++)
			{
				sync[0] &= sync[i]; // save if any are missing
			}
		}
		catch (NoSuchFileException e)
		{
			sync[0] = false; // create config file (this is not an error)
		}
		catch (Exception e)
		{
			JourneyPAC.LOGGER.error("Error loading configs", e);
			sync[0] = false; // restore a valid config
		}
		if (!sync[0])
		{
			JourneyPAC.LOGGER.info("Saving desynced configs");
			save();
		}
	}
	
	public void save()
	{
		try
		{
			ArrayList<String> lines = new ArrayList<>();
			for (ConfigEntry cfg : CONFIGS)
			{
				if (cfg.getInfo().getDescription() != null)
				{
					cfg.getInfo().getDescription().lines().map(s -> '#' + s).forEach(lines::add);
				}
				if (cfg.getInfo().getMinimumValue() != null || cfg.getInfo().getMaximumValue() != null)
				{
					lines.add("#Range: " + cfg.getInfo().getMinimumValue() + " ~ " + cfg.getInfo().getMaximumValue());
				}
				lines.add(cfg.getInfo().getName() + " = " + cfg.save(this));
			}
			lines.add("");
			Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception e)
		{
			JourneyPAC.LOGGER.error("Error saving configs", e);
		}
	}
	
	public boolean getShowClaims()
	{
		return showClaims;
	}
	
	public void setShowClaims(boolean show)
	{
		if (showClaims != show)
		{
			showClaims = show;
			Minecraft.getInstance().execute(this::save);
		}
	}
	
	public boolean getShowForceloads()
	{
		return showForceloads;
	}
	
	public boolean getShowClaimant()
	{
		return showClaimant;
	}
	
	public double getClaimOpacity()
	{
		return claimOpacity;
	}
	
	public double getForceloadOpacity()
	{
		return forceloadOpacity;
	}
	
	public double getForceloadStroke()
	{
		return forceloadStroke;
	}
	
	public double getValidAreaOpacity()
	{
		return validAreaOpacity;
	}
	
	public void onConfigReload(Runnable func)
	{
		// not supported
	}
}
