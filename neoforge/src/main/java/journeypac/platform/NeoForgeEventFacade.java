package journeypac.platform;

import java.util.function.Consumer;

import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class NeoForgeEventFacade implements EventFacade
{
	public void onMousePre(OnMousePre func)
	{
		Consumer<InputEvent.MouseButton.Pre> wrapper = event -> func.onMousePre(event.getAction(), event.getButton());
		NeoForge.EVENT_BUS.addListener(wrapper);
	}
}
