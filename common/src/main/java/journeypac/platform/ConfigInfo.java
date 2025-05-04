package journeypac.platform;

public final class ConfigInfo<T>
{
	private String name;
	private String description;
	private boolean mutable;
	private T initValue;
	private T minValue;
	private T maxValue;
	
	protected ConfigInfo(String name, String description, boolean mutable, T initValue, T minValue, T maxValue)
	{
		this.name = name;
		this.description = description;
		this.mutable = mutable;
		this.initValue = initValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public boolean isMutable()
	{
		return mutable;
	}
	
	public T getInitialValue()
	{
		return initValue;
	}
	
	public T getMinimumValue()
	{
		return minValue;
	}
	
	public T getMaximumValue()
	{
		return maxValue;
	}
}
