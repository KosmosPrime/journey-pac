package journeypac.platform;

public interface ConfigFacade
{
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
