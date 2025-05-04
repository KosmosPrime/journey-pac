package journeypac.platform;

public interface ConfigFacade
{
	public static final ConfigInfo<Boolean> SHOW_CLAIMS = new ConfigInfo<>("show_claims",
			"Whether claims are currently shown, toggleable from the fullscreen map",
			true, true, null, null);
	
	public static final ConfigInfo<Boolean> SHOW_FORCELOADS = new ConfigInfo<>("show_forceloads",
			"Whether forceloaded chunks are marked with an outline (only if claims are shown)",
			false, true, null, null);
	
	public static final ConfigInfo<Boolean> SHOW_CLAIMANT = new ConfigInfo<>("show_claimant",
			"Whether claim owners are shown when hovering over a chunk",
			false, true, null, null);
	
	public static final ConfigInfo<Double> CLAIM_OPACITY = new ConfigInfo<>("claim_opacity",
			"Opacity of a claim on the map (0 is invisible)",
			false, 0.25, 0.0, 1.0);
	
	public static final ConfigInfo<Double> FORCELOAD_OPACITY = new ConfigInfo<>("forceload_opacity",
			"Opacity of the forceload marker (0 is invisible)",
			false, 1.0, 0.0, 1.0);
	
	public static final ConfigInfo<Double> FORCELOAD_STROKE = new ConfigInfo<>("forceload_stroke",
			"Thickness of the forceload marker (in pixels)",
			false, 2.0, 0.0, 16.0);
	
	public static final ConfigInfo<Double> VALID_AREA_OPACITY = new ConfigInfo<>("valid_area_opacity",
			"Opacity of the claimable area outline (0 is invisible)",
			true, 0.5, 0.0, 1.0);
	
	public boolean getShowClaims();
	
	public void setShowClaims(boolean show);
	
	public boolean getShowForceloads();
	
	public boolean getShowClaimant();
	
	public double getClaimOpacity();
	
	public double getForceloadOpacity();
	
	public double getForceloadStroke();
	
	public double getValidAreaOpacity();
	
	public void onConfigReload(Runnable func);
}
