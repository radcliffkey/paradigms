package klic.radoslav.functional.transformer;

import java.util.function.Function;

public class ToStringTransformer<T extends Object>  implements Function<T, String> {

	@Override
	public String apply(T obj) {
		return obj.toString();
	}

}
