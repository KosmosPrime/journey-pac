package journeypac.platform;

import journeymap.client.api.display.ThemeButtonDisplay;
import journeymap.client.api.model.IFullscreen;

public interface EventFacade
{
	@FunctionalInterface
	public interface OnMousePre
	{
		public void onMousePre(int action, int button);
	}
	
	public void onMousePre(OnMousePre func);
	
	@FunctionalInterface
	public interface OnAddonButtonDisplay
	{
		public void onAddonButtonDisplay(IFullscreen fullscreen, ThemeButtonDisplay themeButtonDisplay);
	}
	
	public void onAddonButtonDisplay(OnAddonButtonDisplay func);
}
