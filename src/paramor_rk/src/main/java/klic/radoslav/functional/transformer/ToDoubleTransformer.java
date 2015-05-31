package klic.radoslav.functional.transformer;

import java.util.function.Function;

public class ToDoubleTransformer implements Function<String, Double> {

	@Override
	public Double apply(String str) {	
		return Double.valueOf(str);
	}

}
