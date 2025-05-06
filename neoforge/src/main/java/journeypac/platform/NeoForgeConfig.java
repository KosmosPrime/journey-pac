package journeypac.platform;

import java.util.HashSet;
import java.util.Set;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;

public class NeoForgeConfig implements ConfigFacade
{
	public static final NeoForgeConfig CONFIG;
	public static final ModConfigSpec SPEC;
	
	static
	{
		var configPair = new ModConfigSpec.Builder().configure(NeoForgeConfig::new);
		CONFIG = configPair.getLeft();
		SPEC = configPair.getRight();
	}
	
	private static BooleanValue makeBoolean(ConfigInfo<Boolean> info, ModConfigSpec.Builder builder)
	{
		if (info.getDescription() != null) builder.comment(info.getDescription());
		if (!info.isMutable()) builder.worldRestart();
		return builder.define(info.getName(), info.getInitialValue().booleanValue());
	}
	
	private static DoubleValue makeDouble(ConfigInfo<Double> info, ModConfigSpec.Builder builder)
	{
		if (info.getDescription() != null) builder.comment(info.getDescription());
		if (!info.isMutable()) builder.worldRestart();
		double min = info.getMinimumValue() != null ? info.getMinimumValue() : Double.NEGATIVE_INFINITY;
		double max = info.getMaximumValue() != null ? info.getMaximumValue() : Double.POSITIVE_INFINITY;
		return builder.defineInRange(info.getName(), info.getInitialValue().doubleValue(), min, max);
	}
	
	private final BooleanValue showClaims;
	private final BooleanValue showForceloads;
	private final BooleanValue showClaimant;
	private final DoubleValue claimOpacity;
	private final DoubleValue forceloadOpacity;
	private final DoubleValue forceloadStroke;
	private final DoubleValue validAreaOpacity;
	
	private NeoForgeConfig(ModConfigSpec.Builder builder)
	{
		showClaims = makeBoolean(ConfigFacade.SHOW_CLAIMS, builder);
		showForceloads = makeBoolean(ConfigFacade.SHOW_FORCELOADS, builder);
		showClaimant = makeBoolean(ConfigFacade.SHOW_CLAIMANT, builder);
		claimOpacity = makeDouble(ConfigFacade.CLAIM_OPACITY, builder);
		forceloadOpacity = makeDouble(ConfigFacade.FORCELOAD_OPACITY, builder);
		forceloadStroke = makeDouble(ConfigFacade.FORCELOAD_STROKE, builder);
		validAreaOpacity = makeDouble(ConfigFacade.VALID_AREA_OPACITY, builder);
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
