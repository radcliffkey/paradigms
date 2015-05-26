package cz.klic.test;

import java.util.Arrays;

public class TestSplit {
	
	public static void main(String[] args) {
		String s = "kldkf,df ,koe ,dfkd , ldkfe";
		System.out.println(s.split("\\s*,\\s*").length);
		System.out.println(Arrays.asList(s.split("\\s*,\\s*")));
		s = "kldkf,df ,koe  + dfkd , ldkfe";
		System.out.println(s.split("\\+").length);
		System.out.println(Arrays.asList(s.split("\\+")));
		s = " bla";
		System.out.println(s.split(",").length);
		System.out.println(Arrays.asList(s.split(",")));
	}
	
}
