package cz.klic.functional.transformer;

public class ToDoubleTransformer implements Transformer<String, Double> {

	@Override
	public Double transform(String str) {	
		return Double.valueOf(str);
	}

}
