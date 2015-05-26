package klic.radoslav.settings;

import java.util.HashMap;
import java.util.Map;

public class Settings {

	private static class ValWrapper<T> {
		private T value;

		public ValWrapper(T value) {
			this.value = value;
		}
		
		public T value() {
			return this.value;
		}
		
	}
	
	/**
	 * It is not possible to instantiate HashMap<String, ?>, that's why I use the wrapper class
	 */
	private static Map<String, ValWrapper<?>> settingMap = new HashMap<String, ValWrapper<?>>();
	
	public static <T> void setOption(String name, T value) {
		settingMap.put(name, new ValWrapper<T>(value));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getOption(String name) {
		ValWrapper<?> wrapper = settingMap.get(name);
		return wrapper != null ? (T) settingMap.get(name).value() : null;
	}
	
}
