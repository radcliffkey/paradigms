package klic.radoslav.functional.transformer;


public class LowerCaser implements SingleTypeTransformer<String> {

	@Override
	public String apply(String obj) {
		return obj.toLowerCase();
	}

}
