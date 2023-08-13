package journeypac.platform;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public class JPACConfig implements ConfigFacade
{
	public static final JPACConfig CONFIG;
	public static final ForgeConfigSpec SPEC;
	
	static
	{
		var configPair = new ForgeConfigSpec.Builder().configure(JPACConfig::new);
		CONFIG = configPair.getLeft();
		SPEC = configPair.getRight();
	}
	
	private final BooleanValue showClaims;
	private final BooleanValue showForceloads;
	private final BooleanValue showClaimant;
	private final DoubleValue claimOpacity;
	private final DoubleValue forceloadOpacity;
	private final DoubleValue forceloadStroke;
	private final DoubleValue validAreaOpacity;
	
	private JPACConfig(ForgeConfigSpec.Builder builder)
	{
		showClaims = builder.comment("Whether claims are currently shown, toggleable from the fullscreen map")
				.define("show_claims", true);
		// these use worldRestart to avoid inconsistencies as the polygons are cached
		showForceloads = builder.comment("Whether forceloaded chunks are marked with an outline (only if claims are shown)")
				.worldRestart()
				.define("show_forceloads", true);
		showClaimant = builder.comment("Whether claim owners are shown when hovering over a chunk")
				.worldRestart()
				.define("show_claimant", true);
		claimOpacity = builder.comment("Opacity of a claim on the map (0 is invisible)")
				.worldRestart()
				.defineInRange("claim_opacity", 0.25, 0, 1);
		forceloadOpacity = builder.comment("Opacity of the forceload marker (0 is invisible)")
				.worldRestart()
				.defineInRange("forceload_opacity", 1.0, 0, 1);
		forceloadStroke = builder.comment("Thickness of the forceload marker (in pixels)")
				.worldRestart()
				.defineInRange("forceload_stroke", 2.0, 0, 16);
		validAreaOpacity = builder.comment("Opacity of the claimable area outline (0 is invisible)")
				.defineInRange("valid_area_opacity", 0.5, 0, 1);
	}
	
	public boolean getShowClaims()
	{
		return showClaims.get();
	}
	
	public void setShowClaims(boolean show)
	{
		showClaims.set(show);
		showClaims.save();
	}
	
	public boolean getShowForceloads()
	{
		return showForceloads.get();
	}
	
	public boolean getShowClaimant()
	{
		return showClaimant.get();
	}
	
	public double getClaimOpacity()
	{
		return claimOpacity.get();
	}
	
	public double getForceloadOpacity()
	{
		return forceloadOpacity.get();
	}
	
	public double getForceloadStroke()
	{
		return forceloadStroke.get();
	}
	
	public double getValidAreaOpacity()
	{
		return validAreaOpacity.get();
	}
	
	private Set<Runnable> onConfigReload = new HashSet<>();
	
	public void fireConfigReload()
	{
		onConfigReload.forEach(Runnable::run);
	}
	
	public void onConfigReload(Runnable func)
	{
		onConfigReload.add(func);
	}
}
