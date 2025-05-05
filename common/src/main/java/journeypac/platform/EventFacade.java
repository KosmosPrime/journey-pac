package journeypac.platform;

public interface EventFacade
{
	@FunctionalInterface
	public interface OnMousePre
	{
		public void onMousePre(int action, int button);
	}
	
	public void onMousePre(OnMousePre func);
}
