package journeypac.platform;

import com.mojang.blaze3d.platform.InputConstants;

import journeymap.client.api.event.fabric.FabricEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public final class FabricEventFacade implements EventFacade
{
	public void onMousePre(OnMousePre func)
	{
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
		{
			ScreenMouseEvents.beforeMouseClick(screen).register((s, x, y, button) ->
				func.onMousePre(InputConstants.PRESS, button));
			ScreenMouseEvents.beforeMouseRelease(screen).register((s, x, y, button) ->
				func.onMousePre(InputConstants.RELEASE, button));
		});
	}
	
	public void onAddonButtonDisplay(OnAddonButtonDisplay func)
	{
		FabricEvents.ADDON_BUTTON_DISPLAY_EVENT.register(
			event -> func.onAddonButtonDisplay(event.getFullscreen(), event.getThemeButtonDisplay()));
	}
}
