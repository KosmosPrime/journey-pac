package journeypac;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.IThemeButton;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.display.ThemeButtonDisplay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.DisplayUpdateEvent;
import journeymap.client.api.event.FullscreenMapEvent.ClickEvent;
import journeymap.client.api.event.FullscreenMapEvent.MouseDraggedEvent;
import journeymap.client.api.event.FullscreenMapEvent.MouseMoveEvent;
import journeymap.client.api.event.FullscreenMapEvent.Stage;
import journeymap.client.api.event.forge.FullscreenDisplayEvent.AddonButtonDisplayEvent;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.UIState;
import journeypac.KeyMappings.ClaimMode;
import journeypac.platform.ConfigFacade;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
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
	private ConfigFacade config;
	private KeyMappings keyMap;
	
	private ResourceKey<Level> dimension;
	private boolean showClaims;
	private Map<Long, PolygonOverlay[]> claimMap = new HashMap<>();
	
	private ClaimMode areaMode;
	private boolean areaAdd;
	private int areaStartX;
	private int areaStartZ;
	private PolygonOverlay areaOverlay;
	// for overlay caching only
	private int areaEndX;
	private int areaEndZ;
	
	private PolygonOverlay validOverlay;
	private int validCenterX;
	private int validCenterZ;
	private int validRange;
	
	public JourneymapPlugin()
	{
		JourneyPAC mod = JourneyPAC.getInstance();
		config = mod.getConfig();
		keyMap = mod.getKeyMappings();
		MinecraftForge.EVENT_BUS.addListener(this::onMousePre);
		config.onConfigReload(this::onConfigReload);
		showClaims = config.getShowClaims();
	}
	
	// use pre event because the fullscreen map consumes all
	private void onMousePre(InputEvent.MouseButton.Pre event)
	{
		if (event.getAction() == InputConstants.RELEASE && areaMode != null)
		{
			var claimsManager = opacApi.getClaimsManager();
			if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && areaAdd)
			{
				// don't cancel, this is the raw event not the JM event equivalent we canceled
				if (areaEndX == areaStartX && areaEndZ == areaStartZ)
				{
					// single chunk claim (wasn't dragged or only one chunk)
					switch (areaMode)
					{
						case CLAIM:
							claimsManager.requestClaim(areaStartX, areaStartZ, claimsManager.isServerMode());
							break;
						case FORCELOAD:
							claimsManager.requestForceload(areaStartX, areaStartZ, true, claimsManager.isServerMode());
							break;
						default:
							JourneyPAC.LOGGER.warn("Unhandled chunk claim mode " + areaMode);
					}
				}
				else
				{
					// area claim (more than 1 chunk selected)
					int x0 = Math.min(areaStartX, areaEndX), z0 = Math.min(areaStartZ, areaEndZ);
					int x1 = Math.max(areaStartX, areaEndX), z1 = Math.max(areaStartZ, areaEndZ);
					switch (areaMode)
					{
						case CLAIM:
							claimsManager.requestAreaClaim(x0, z0, x1, z1, claimsManager.isServerMode());
							break;
						case FORCELOAD:
							claimsManager.requestAreaForceload(x0, z0, x1, z1, true, claimsManager.isServerMode());
							break;
						default:
							JourneyPAC.LOGGER.warn("Unhandled area claim mode " + areaMode);
					}
				}
				
				// done with the active claim, clean up
				areaMode = null;
				if (areaOverlay != null)
				{
					jmApi.remove(areaOverlay);
					areaOverlay = null;
				}
				if (validOverlay != null)
				{
					jmApi.remove(validOverlay);
					validOverlay = null;
				}
			}
			else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !areaAdd)
			{
				// don't cancel, this is the raw event not the JM event equivalent we canceled
				if (areaEndX == areaStartX && areaEndZ == areaStartZ)
				{
					// single chunk unclaim (wasn't dragged or only one chunk)
					switch (areaMode)
					{
						case CLAIM:
							claimsManager.requestUnclaim(areaStartX, areaStartZ, claimsManager.isServerMode());
							break;
						case FORCELOAD:
							claimsManager.requestForceload(areaStartX, areaStartZ, false, claimsManager.isServerMode());
							break;
						default:
							JourneyPAC.LOGGER.warn("Unhandled chunk unclaim mode " + areaMode);
					}
					
				}
				else
				{
					// area unclaim (more than 1 chunk selected)
					int x0 = Math.min(areaStartX, areaEndX), z0 = Math.min(areaStartZ, areaEndZ);
					int x1 = Math.max(areaStartX, areaEndX), z1 = Math.max(areaStartZ, areaEndZ);
					switch (areaMode)
					{
						case CLAIM:
							claimsManager.requestAreaUnclaim(x0, z0, x1, z1, claimsManager.isServerMode());
							break;
						case FORCELOAD:
							claimsManager.requestAreaForceload(x0, z0, x1, z1, false, claimsManager.isServerMode());
							break;
						default:
							JourneyPAC.LOGGER.warn("Unhandled area unclaim mode " + areaMode);
					}
				}
				
				// done with the active claim, clean up
				areaMode = null;
				if (areaOverlay != null)
				{
					jmApi.remove(areaOverlay);
					areaOverlay = null;
				}
				if (validOverlay != null)
				{
					jmApi.remove(validOverlay);
					validOverlay = null;
				}
			}
		}
	}
	
	private void onConfigReload()
	{
		boolean newShowClaims = config.getShowClaims();
		if (newShowClaims != showClaims)
		{
			showClaims = newShowClaims;
			if (showClaims) showClaims();
			else hideClaims();
		}
	}
	
	public String getModId()
	{
		return JourneyPAC.MODID;
	}
	
	public void initialize(IClientAPI jmApi)
	{
		this.jmApi = jmApi;
		jmApi.subscribe(getModId(), EnumSet.of(ClientEvent.Type.DISPLAY_UPDATE, ClientEvent.Type.MAPPING_STARTED,
				ClientEvent.Type.MAPPING_STOPPED, ClientEvent.Type.MAP_CLICKED, ClientEvent.Type.MAP_DRAGGED,
				ClientEvent.Type.MAP_MOUSE_MOVED));
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
			config.setShowClaims(showClaims);
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
		if (config.getShowClaimant())
		{
			// get the name for this claim
			String playerName = playerInfo.getPlayerUsername();
			String subName = playerInfo.getClaimsName(claim.getSubConfigIndex());
			String name = subName != null ? subName : playerInfo.getClaimsName();
			trueName = name == null || name.isEmpty() ? playerName : playerName + " / " + name;
		}
		// generate the polygon overlay for this chunk
		ShapeProperties shape = new ShapeProperties().setFillColor(color)
				.setFillOpacity((float) config.getClaimOpacity());
		if (config.getShowForceloads() && claim.isForceloadable())
		{
			shape.setStrokeColor(color)
					.setStrokeOpacity((float) config.getForceloadOpacity())
					.setStrokeWidth((float) config.getForceloadStroke());
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
				case DISPLAY_UPDATE:
				{
					UIState display = ((DisplayUpdateEvent) event).uiState;
					if (display.ui == Context.UI.Fullscreen && !display.active)
					{
						areaMode = null;
						if (areaOverlay != null)
						{
							jmApi.remove(areaOverlay);
							areaOverlay = null;
						}
					}
					break;
				}
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
				case MAP_CLICKED:
				{
					ClickEvent clickEvent = (ClickEvent) event;
					if (dimension != null && event.dimension == dimension)
					{
						if (clickEvent.getStage() == Stage.PRE && areaMode == null &&
								(clickEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT ||
										clickEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
						{
							ClaimMode mode = keyMap.getClaimMode();
							if (mode != ClaimMode.NONE)
							{
								clickEvent.cancel();
								areaMode = mode;
								areaAdd = clickEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
								areaStartX = SectionPos.blockToSectionCoord(clickEvent.getLocation().getX());
								areaStartZ = SectionPos.blockToSectionCoord(clickEvent.getLocation().getZ());
								areaEndX = areaStartX;
								areaEndZ = areaStartZ;
							}
						}
					}
					break;
				}
				case MAP_DRAGGED:
				{
					MouseDraggedEvent dragEvent = (MouseDraggedEvent) event;
					if (dragEvent.getStage() == Stage.PRE && areaMode != null &&
							((dragEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && areaAdd) ||
									(dragEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !areaAdd)))
					{
						// cancel dragging events (which keep appearing) while we're area-claiming
						dragEvent.cancel();
					}
					break;
				}
				case MAP_MOUSE_MOVED:
				{
					MouseMoveEvent moveEvent = (MouseMoveEvent) event;
					if (areaMode != null)
					{
						// create or update the claim preview
						int currX = SectionPos.blockToSectionCoord(moveEvent.getLocation().getX());
						int currZ = SectionPos.blockToSectionCoord(moveEvent.getLocation().getZ());
						if (areaOverlay == null || currX != areaEndX || currZ != areaEndZ)
						{
							areaEndX = currX;
							areaEndZ = currZ;
							int x0 = SectionPos.sectionToBlockCoord(Math.min(currX, areaStartX));
							int z0 = SectionPos.sectionToBlockCoord(Math.min(currZ, areaStartZ));
							int x1 = SectionPos.sectionToBlockCoord(Math.max(currX, areaStartX) + 1);
							int z1 = SectionPos.sectionToBlockCoord(Math.max(currZ, areaStartZ) + 1);
							MapPolygon area = new MapPolygon(new BlockPos(x0, 0, z1), new BlockPos(x1, 0, z1),
									new BlockPos(x1, 0, z0), new BlockPos(x0, 0, z0));
							if (areaOverlay == null)
							{
								ShapeProperties shape = new ShapeProperties()
										.setStrokeColor(0xFFFFFF).setFillColor(0xFFFFFF)
										.setFillOpacity((float) config.getClaimOpacity());
								areaOverlay = new PolygonOverlay(getModId(), "claim_area", dimension, shape, area);
							}
							else
							{
								areaOverlay.setOuterArea(area);
								areaOverlay.flagForRerender();
							}
							jmApi.show(areaOverlay);
						}
						
						float validOpacity = (float) config.getValidAreaOpacity();
						if (validOpacity > 0)
						{
							// only update on mouse move, you normally won't move while in the fullscreen map
							Minecraft mc = Minecraft.getInstance();
							if (mc.player != null)
							{
								ChunkPos chunk = mc.player.chunkPosition();
								int chunkX = chunk.x, chunkZ = chunk.z;
								int range = opacApi.getClaimsManager().getMaxClaimDistance();
								if (validOverlay == null || (chunkX != validCenterX) || (chunkZ != validCenterZ) || (range != validRange))
								{
									// extend by 1 block to avoid drawing in the same place as the claim rectangle
									int x0 = SectionPos.sectionToBlockCoord(chunkX - range) - 1;
									int z0 = SectionPos.sectionToBlockCoord(chunkZ - range) - 1;
									int x1 = SectionPos.sectionToBlockCoord(chunkX + range + 1) + 1;
									int z1 = SectionPos.sectionToBlockCoord(chunkZ + range + 1) + 1;
									MapPolygon area = new MapPolygon(new BlockPos(x0, 0, z1), new BlockPos(x1, 0, z1),
											new BlockPos(x1, 0, z0), new BlockPos(x0, 0, z0));
									if (validOverlay == null)
									{
										ShapeProperties shape = new ShapeProperties().setStrokeColor(0xBFBFBF)
												.setStrokeOpacity(validOpacity).setFillOpacity(0).setStrokeWidth(2);
										validOverlay = new PolygonOverlay(getModId(), "valid_claim_area", dimension, shape, area);
									}
									else
									{
										validOverlay.setOuterArea(area);
										validOverlay.flagForRerender();
									}
									jmApi.show(validOverlay);
								}
							}
						}
					}
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
