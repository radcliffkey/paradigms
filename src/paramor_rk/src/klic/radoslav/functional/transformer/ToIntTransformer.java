package klic.radoslav.functional.transformer;

public class ToIntTransformer implements Transformer<String, Integer> {

	@Override
	public Integer transform(String str) {	
		return Integer.valueOf(str);
	}

}
