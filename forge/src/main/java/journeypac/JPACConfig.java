package journeypac;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public class JPACConfig
{
	public static final JPACConfig CONFIG;
	public static final ForgeConfigSpec SPEC;
	
	static
	{
		var configPair = new ForgeConfigSpec.Builder().configure(JPACConfig::new);
		CONFIG = configPair.getLeft();
		SPEC = configPair.getRight();
	}
	
	public final BooleanValue showClaims;
	public final BooleanValue showForceloads;
	public final BooleanValue showClaimant;
	public final DoubleValue claimOpacity;
	public final DoubleValue forceloadOpacity;
	public final DoubleValue forceloadStroke;
	public final DoubleValue validAreaOpacity;
	
	protected JPACConfig(ForgeConfigSpec.Builder builder)
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
}
