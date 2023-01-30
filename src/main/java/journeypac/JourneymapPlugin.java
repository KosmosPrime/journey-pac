package journeypac;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.IThemeButton;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.display.ThemeButtonDisplay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.forge.FullscreenDisplayEvent.AddonButtonDisplayEvent;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.client.claims.api.IClientDimensionClaimsManagerAPI;
import xaero.pac.client.claims.api.IClientRegionClaimsAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;

@ClientPlugin
public class JourneymapPlugin implements IClientPlugin
{
	public static final int REGION_BITS = 5;
	public static final int REGION_SIZE = 1 << REGION_BITS;
	public static final int REGION_MASK = REGION_SIZE - 1;
	
	public static final long getRegionIndex(int regionX, int regionZ)
	{
		return (regionX & 0xFFFFFFFFl) | ((regionZ & 0xFFFFFFFFl) << Integer.SIZE);
	}
	
	public static final int getSubRegionIndex(int chunkX, int chunkZ)
	{
		return (chunkX & REGION_MASK) | ((chunkZ & REGION_MASK) << REGION_BITS);
	}
	
	private IClientAPI jmApi;
	private OpenPACClientAPI opacApi;
	
	private ResourceKey<Level> dimension;
	private boolean showClaims = JPACConfig.CONFIG.showClaims.get();
	private Map<Long, PolygonOverlay[]> claimMap = new HashMap<>();
	
	public JourneymapPlugin()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
	}
	
	private void onConfigReload(ModConfigEvent.Reloading event)
	{
		if (event.getConfig().getSpec() == JPACConfig.SPEC)
		{
			boolean newShowClaims = JPACConfig.CONFIG.showClaims.get();
			if (newShowClaims != showClaims)
			{
				showClaims = newShowClaims;
				if (showClaims) showClaims();
				else hideClaims();
			}
		}
	}
	
	public String getModId()
	{
		return JourneyPAC.MODID;
	}
	
	public void initialize(IClientAPI jmApi)
	{
		this.jmApi = jmApi;
		jmApi.subscribe(getModId(), EnumSet.of(ClientEvent.Type.MAPPING_STARTED, ClientEvent.Type.MAPPING_STOPPED));
		MinecraftForge.EVENT_BUS.addListener(this::onAddonButtonDisplayEvent);
		
		opacApi = OpenPACClientAPI.get();
		opacApi.getClaimsManager().getTracker().register(new IClaimsManagerListenerAPI()
		{
			public void onChunkChange(ResourceLocation dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim)
			{
				try
				{
					ResourceKey<Level> curr = JourneymapPlugin.this.dimension;
					if (curr != null && curr.location().equals(dimension))
					{
						JourneyPAC.LOGGER.debug("Updating chunk " + chunkX + " " + chunkZ + " in " + dimension);
						int regionX = chunkX >> REGION_BITS, regionZ = chunkZ >> REGION_BITS;
						Long regionPos = getRegionIndex(regionX, regionZ);
						int subRegionPos = getSubRegionIndex(chunkX, chunkZ);
						PolygonOverlay[] inRegion = claimMap.get(regionPos);
						PolygonOverlay overlay = inRegion != null ? inRegion[subRegionPos] : null;
						if (overlay != null)
						{
							jmApi.remove(overlay);
							inRegion[subRegionPos] = null;
						}
						if (claim != null)
						{
							if (inRegion == null)
							{
								inRegion = new PolygonOverlay[REGION_SIZE * REGION_SIZE];
								claimMap.put(regionPos, inRegion);
							}
							overlay = makeClaim(chunkX, chunkZ, claim);
							inRegion[subRegionPos] = overlay;
							if (showClaims)
							{
								try
								{
									jmApi.show(overlay);
								}
								catch (Exception e)
								{
									JourneyPAC.LOGGER.error("Error displaying claimed chunks", e);
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					JourneyPAC.LOGGER.error("Error computing chunk changes", e);
				}
			}
			
			public void onWholeRegionChange(ResourceLocation dimension, int regionX, int regionZ)
			{
				try
				{
					ResourceKey<Level> curr = JourneymapPlugin.this.dimension;
					if (curr != null && curr.location().equals(dimension))
					{
						JourneyPAC.LOGGER.debug("Updating region " + regionX + " " + regionZ + " in " + dimension);
						var dimClaims = opacApi.getClaimsManager().getDimension(dimension);
						if (dimClaims != null) buildRegion(dimClaims.getRegion(regionX, regionZ));
					}
				}
				catch (Exception e)
				{
					JourneyPAC.LOGGER.error("Error computing region changes", e);
				}
			}
			
			public void onDimensionChange(ResourceLocation dimension)
			{
				try
				{
					ResourceKey<Level> curr = JourneymapPlugin.this.dimension;
					if (curr != null && curr.location().equals(dimension))
					{
						JourneyPAC.LOGGER.debug("Updating dimension " + dimension);
						// total rebuild, make sure to remove current claims
						hideClaims();
						claimMap.clear();
						buildDimension(opacApi.getClaimsManager().getDimension(dimension));
						showClaims();
					}
				}
				catch (Exception e)
				{
					JourneyPAC.LOGGER.error("Error computing dimension changes", e);
				}
			}
		});
	}
	
	private void onAddonButtonDisplayEvent(AddonButtonDisplayEvent event)
	{
		ThemeButtonDisplay display = event.getThemeButtonDisplay();
		display.addThemeToggleButton("button.journeypac.toggle_claims", "opac", showClaims, this::onToggleClaims);
	}
	
	private void onToggleClaims(IThemeButton btn)
	{
		try
		{
			showClaims = btn.getToggled() != Boolean.TRUE;
			JPACConfig.CONFIG.showClaims.set(showClaims);
			JPACConfig.CONFIG.showClaims.save();
			btn.setToggled(showClaims);
			if (!claimMap.isEmpty())
			{
				if (showClaims) showClaims();
				else hideClaims();
			}
		}
		catch (Exception e)
		{
			JourneyPAC.LOGGER.error("Error toggling claim display", e);
		}
	}
	
	private void buildDimension(IClientDimensionClaimsManagerAPI<?> dimClaims)
	{
		claimMap.clear();
		if (dimClaims != null && dimClaims.getCount() > 0)
		{
			dimClaims.getRegionStream().forEach(this::buildRegion);
		}
	}
	
	private void buildRegion(IClientRegionClaimsAPI regionClaims)
	{
		if (regionClaims != null)
		{
			Long regionPos = regionClaims.getX() & 0xFFFFFFFFl | (regionClaims.getZ() & 0xFFFFFFFFl) << 32;
			PolygonOverlay[] inRegion = claimMap.get(regionPos);
			// remove the current data for this region
			if (inRegion != null)
			{
				for (PolygonOverlay curr : inRegion)
				{
					jmApi.remove(curr);
				}
				Arrays.fill(inRegion, null);
			}
			
			// generate region data, have to iterate over all chunks because the API doesn't have a stream variant
			int startX = regionClaims.getX() << REGION_BITS, startZ = regionClaims.getZ() << REGION_BITS;
			boolean any = false;
			for (int z = 0; z < REGION_SIZE; z++)
			{
				for (int x = 0; x < REGION_SIZE; x++)
				{
					IPlayerChunkClaimAPI claim = regionClaims.get(x, z);
					if (claim != null)
					{
						any = true;
						PolygonOverlay overlay = makeClaim(startX + x, startZ + z, claim);
						if (inRegion == null)
						{
							inRegion = new PolygonOverlay[REGION_SIZE * REGION_SIZE];
							claimMap.put(regionPos, inRegion);
						}
						inRegion[getSubRegionIndex(x, z)] = overlay;
					}
				}
			}
			if (!any && inRegion != null) claimMap.remove(regionPos);
		}
	}
	
	// REM merge chunks to reduce the poly count
	private PolygonOverlay makeClaim(int chunkX, int chunkZ, IPlayerChunkClaimAPI claim)
	{
		var playerInfo = opacApi.getClaimsManager().getPlayerInfo(claim.getPlayerId());
		// get the color for this claim
		Integer subColor = playerInfo.getClaimsColor(claim.getSubConfigIndex());
		int color = subColor != null ? subColor : playerInfo.getClaimsColor();
		color &= 0xFFFFFF;
		String trueName = null;
		if (JPACConfig.CONFIG.showClaimant.get())
		{
			// get the name for this claim
			String playerName = playerInfo.getPlayerUsername();
			String subName = playerInfo.getClaimsName(claim.getSubConfigIndex());
			String name = subName != null ? subName : playerInfo.getClaimsName();
			trueName = name == null || name.isEmpty() ? playerName : playerName + " / " + name;
		}
		// generate the polygon overlay for this chunk
		ShapeProperties shape = new ShapeProperties().setFillColor(color)
				.setFillOpacity(JPACConfig.CONFIG.claimOpacity.get().floatValue());
		if (JPACConfig.CONFIG.showForceloads.get() && claim.isForceloadable())
		{
			shape.setStrokeColor(color)
					.setStrokeOpacity(JPACConfig.CONFIG.forceloadOpacity.get().floatValue())
					.setStrokeWidth(JPACConfig.CONFIG.forceloadStroke.get().floatValue());
		}
		else shape.setStrokeOpacity(0);
		int x0 = chunkX << 4, z0 = chunkZ << 4, x1 = x0 + 16, z1 = z0 + 16;
		MapPolygon area = new MapPolygon(new BlockPos(x0, 0, z1), new BlockPos(x1, 0, z1),
				new BlockPos(x1, 0, z0), new BlockPos(x0, 0, z0));
		PolygonOverlay overlay = new PolygonOverlay(getModId(), "claim_" + chunkX + "_" + chunkZ, dimension, shape, area);
		if (trueName != null)
		{
			overlay.setTitle(trueName);
			overlay.setTextProperties(new TextProperties().setColor(color));
		}
		return overlay;
	}
	
	private void showClaims()
	{
		if (!claimMap.isEmpty())
		{
			try
			{
				for (PolygonOverlay[] inRegion : claimMap.values())
				{
					for (PolygonOverlay curr : inRegion)
					{
						if (curr != null) jmApi.show(curr);
					}
				}
			}
			catch (Exception e)
			{
				JourneyPAC.LOGGER.error("Error displaying claimed chunks", e);
			}
		}
	}
	
	private void hideClaims()
	{
		if (!claimMap.isEmpty())
		{
			for (PolygonOverlay[] inRegion : claimMap.values())
			{
				for (PolygonOverlay curr : inRegion)
				{
					if (curr != null) jmApi.remove(curr);
				}
			}
		}
	}
	
	public void onEvent(ClientEvent event)
	{
		try
		{
			switch (event.type)
			{
				case MAPPING_STARTED:
				{
					if (dimension != null)
					{
						JourneyPAC.LOGGER.warn("Started mapping " + event.dimension.location()
								+ " but already mapping " + dimension);
					}
					else JourneyPAC.LOGGER.debug("Start mapping " + event.dimension.location());
					dimension = event.dimension;
					// compute claims for this dimension
					buildDimension(opacApi.getClaimsManager().getDimension(dimension.location()));
					// show claims if enabled
					if (showClaims) showClaims();
					break;
				}
				case MAPPING_STOPPED:
				{
					if (dimension == null)
					{
						JourneyPAC.LOGGER.warn("Stopped mapping " + event.dimension.location()
								+ " but never started mapping");
					}
					else if (!dimension.equals(event.dimension))
					{
						JourneyPAC.LOGGER.warn("Stopped mapping " + event.dimension.location()
								+ " but currently mapping " + dimension);
					}
					else JourneyPAC.LOGGER.debug("Stop mapping " + event.dimension.location());
					hideClaims();
					claimMap.clear();
					dimension = null;
					break;
				}
				default:
					// because we subscribe to events, don't get any otherwise
					JourneyPAC.LOGGER.warn("Unhandled event " + event.type);
					break;
			}
		}
		catch (Exception e)
		{
			JourneyPAC.LOGGER.error("Error handling event (" + event.type + ")", e);
		}
	}
}
