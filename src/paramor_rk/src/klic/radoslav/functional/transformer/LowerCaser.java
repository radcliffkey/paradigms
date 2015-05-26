package klic.radoslav.functional.transformer;


public class LowerCaser implements SingleTypeTransformer<String> {

	@Override
	public String transform(String obj) {
		return obj.toLowerCase();
	}

}
