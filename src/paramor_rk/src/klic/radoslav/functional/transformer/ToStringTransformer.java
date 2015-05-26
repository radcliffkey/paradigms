package klic.radoslav.functional.transformer;

public class ToStringTransformer<T extends Object>  implements Transformer<T, String> {

	@Override
	public String transform(T obj) {
		return obj.toString();
	}

}
