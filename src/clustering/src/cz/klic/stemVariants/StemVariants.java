package cz.klic.stemVariants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import cz.klic.util.Fileutil;

public class StemVariants {

	private Map<String, String> simpleToCompound;

	public StemVariants(Map<String, String> simpleToCompound) {
		super();
		this.simpleToCompound = simpleToCompound;
	}
	
	public String getCompound(String stem) {
		return this.simpleToCompound.get(stem);
	}
	
	public boolean isVariant(String stem1, String stem2) {
		String comp1 = this.getCompound(stem1);
		if (comp1 == null) {
			return false;
		}
		String comp2 = this.getCompound(stem2);
		
		return comp1.equals(comp2);
	}
	
	public static StemVariants read(BufferedReader reader) throws IOException {
		Map<String, String> varMap = Fileutil.readMap(reader);
		return new StemVariants(varMap);
	}
}
