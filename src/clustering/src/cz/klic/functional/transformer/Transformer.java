package cz.klic.functional.transformer;

public interface Transformer <FromType, ToType> {
	
	public ToType transform(FromType obj);
	
}
