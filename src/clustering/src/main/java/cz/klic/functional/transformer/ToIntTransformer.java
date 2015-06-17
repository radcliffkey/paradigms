package cz.klic.functional.transformer;

import java.util.function.Function;

public class ToIntTransformer implements Function<String, Integer> {

	@Override
	public Integer apply(String str) {	
		return Integer.valueOf(str);
	}

}
