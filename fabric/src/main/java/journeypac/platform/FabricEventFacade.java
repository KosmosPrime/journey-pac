package journeypac.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public final class FabricEventFacade implements EventFacade
{
	public void onMousePre(OnMousePre func)
	{
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
		{
			ScreenMouseEvents.beforeMouseClick(screen).register((s, e) ->
				func.onMousePre(InputConstants.PRESS, e.button()));
			ScreenMouseEvents.beforeMouseRelease(screen).register((s, e) ->
				func.onMousePre(InputConstants.RELEASE, e.button()));
		});
	}
}
