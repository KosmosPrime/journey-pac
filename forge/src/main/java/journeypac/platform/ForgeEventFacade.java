package journeypac.platform;

import java.util.function.Consumer;

import journeymap.api.v2.common.event.FullscreenEventRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;

import journeypac.JourneyPAC;

public final class ForgeEventFacade implements EventFacade
{
	public void onMousePre(OnMousePre func)
	{
		Consumer<InputEvent.MouseButton.Pre> wrapper = event -> func.onMousePre(event.getAction(), event.getButton());
		MinecraftForge.EVENT_BUS.addListener(wrapper);
	}
	
	public void onAddonButtonDisplay(OnAddonButtonDisplay func)
	{
		FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(JourneyPAC.MODID,
			event -> func.onAddonButtonDisplay(event.getFullscreen(), event.getThemeButtonDisplay()));
	}
}
