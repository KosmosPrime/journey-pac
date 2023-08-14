package journeypac.platform;

import java.util.function.Consumer;

import journeymap.client.api.event.forge.FullscreenDisplayEvent.AddonButtonDisplayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;

public final class ForgeEventFacade implements EventFacade
{
	public void onMousePre(OnMousePre func)
	{
		Consumer<InputEvent.MouseButton.Pre> wrapper = event -> func.onMousePre(event.getAction(), event.getButton());
		MinecraftForge.EVENT_BUS.addListener(wrapper);
	}
	
	public void onAddonButtonDisplay(OnAddonButtonDisplay func)
	{
		Consumer<AddonButtonDisplayEvent> wrapper =
			event -> func.onAddonButtonDisplay(event.getFullscreen(), event.getThemeButtonDisplay());
		MinecraftForge.EVENT_BUS.addListener(wrapper);
	}
}
