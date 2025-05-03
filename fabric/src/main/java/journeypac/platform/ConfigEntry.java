package journeypac.platform;

import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import journeypac.platform.ConfigInfo;

public abstract class ConfigEntry<T>
{
	public static final class BooleanConfig extends ConfigEntry<Boolean>
	{
		private Predicate<FabricConfig> getter;
		private BiConsumer<FabricConfig, Boolean> setter;
		
		public BooleanConfig(ConfigInfo<Boolean> info, Predicate<FabricConfig> getter, BiConsumer<FabricConfig, Boolean> setter)
		{
			super(info);
			this.getter = getter;
			this.setter = setter;
		}
		
		public void reset(FabricConfig config)
		{
			setter.accept(config, info.getInitialValue());
		}
		
		public boolean load(FabricConfig config, String val)
		{
			setter.accept(config, Boolean.parseBoolean(val));
			return true;
		}
		
		public String save(FabricConfig config)
		{
			return Boolean.toString(getter.test(config));
		}
	}
	
	public static final class DoubleConfig extends ConfigEntry<Double>
	{
		private ToDoubleFunction<FabricConfig> getter;
		private ObjDoubleConsumer<FabricConfig> setter;
		
		public DoubleConfig(ConfigInfo<Double> info, ToDoubleFunction<FabricConfig> getter, ObjDoubleConsumer<FabricConfig> setter)
		{
			super(info);
			this.getter = getter;
			this.setter = setter;
		}
		
		public void reset(FabricConfig config)
		{
			setter.accept(config, info.getInitialValue());
		}
		
		public boolean load(FabricConfig config, String val)
		{
			double dbl = Double.parseDouble(val);
			if (info.getMinimumValue() != null && dbl < info.getMinimumValue())
			{
				setter.accept(config, info.getMinimumValue());
				return false;
			}
			if (info.getMaximumValue() != null && dbl > info.getMaximumValue())
			{
				setter.accept(config, info.getMaximumValue());
				return false;
			}
			setter.accept(config, dbl);
			return true;
		}
		
		public String save(FabricConfig config)
		{
			return Double.toString(getter.applyAsDouble(config));
		}
	}
	
	private int index;
	protected final ConfigInfo<T> info;
	
	protected ConfigEntry(ConfigInfo<T> info)
	{
		this.info = info;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	void setIndex(int index)
	{
		this.index = index;
	}
	
	public ConfigInfo<T> getInfo()
	{
		return info;
	}
	
	public abstract void reset(FabricConfig config);
	
	public abstract boolean load(FabricConfig config, String val);
	
	public abstract String save(FabricConfig config);
}
