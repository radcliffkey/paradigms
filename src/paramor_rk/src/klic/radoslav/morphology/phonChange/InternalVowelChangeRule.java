package klic.radoslav.morphology.phonChange;

import klic.radoslav.util.StringUtil;

public class InternalVowelChangeRule extends StemInternalChangeRule {

	public InternalVowelChangeRule(String delta1, String delta2) {
		super(delta1, delta2);
	}
	
	@Override
	public boolean isApplicable(String stem) {
		
		int idx = stem.indexOf(delta1);
		if (idx < 1 || idx == stem.length() - delta1.length()) {
			return false;
		}
		
		char leftNeighbour = stem.charAt(idx - 1);
		char rightNeighbour = stem.charAt(idx + delta1.length());
		
		if (StringUtil.isVowel(leftNeighbour) || StringUtil.isVowel(rightNeighbour)) {
			return false;
		}
		
		return true;
	}
	
}
