package klic.radoslav.morphology.searchAndProcessing;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.morphology.SeedAnalyses;
import klic.radoslav.morphology.schemes.SeedScheme;
import klic.radoslav.util.DebugLog;
import klic.radoslav.util.MathUtil;
import klic.radoslav.util.StringUtil;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster;

public class ManualClusterProtector {

	public static boolean shouldBeKept(BottomUpSearchResultCluster cluster) throws IOException {
		
		if (!ManualData.isManualSeedAvailable()) {
			return false;
		}
		
		SeedAnalyses manualAnalyses = ManualData.getManualAnalyses();
		List<SeedScheme> manualSchemes = manualAnalyses.getSeedSchemes();

		List<BottomUpSearchResultCluster> leaves = cluster.getLeaves();
		//for each manual scheme: how many leaf schemes have intersection at least size 2 
		int [] intersectLeaves= new int[manualSchemes.size()];
		for (BottomUpSearchResultCluster leaf : leaves) {
			Set<Affix> leafAffixes = leaf.getCoveredAffixes();

			int i = 0;
			for (SeedScheme manScheme : manualSchemes) {
				SetOfMorphemes<Affix> manAffixes = manScheme.getAffixes();
				SetOfMorphemes<Affix> intersection = manAffixes.intersect(new SetOfMorphemes<Affix>(leafAffixes));

				intersection.size();
				
				if (intersection.size() >= 2) {
					++intersectLeaves[i];
				}
					
				++i;
			}
		}
		
		int maxIntersectLeaves = MathUtil.max(intersectLeaves);
		if (maxIntersectLeaves > (double)leaves.size() / 2) {
//			DebugLog.write(leaves.size());
//			DebugLog.write(StringUtil.join(", ", intersectLeaves));
			return true;
		}
		return false;

//		if (keep > 0) {
//			return true;
//		} else {
//			return false;
//		}
//		Set<Affix> clusterAffixes = cluster.getCoveredAffixes();
//		int maxIntersect = 0;
//		for (SeedScheme manScheme : manualSchemes) {
//			SetOfMorphemes<Affix> manAffixes = manScheme.getAffixes();
//			SetOfMorphemes<Affix> intersection = manAffixes.intersect(new SetOfMorphemes<Affix>(clusterAffixes));
//			maxIntersect = Math.max(intersection.size(), maxIntersect);
//		}
//		
//		if (maxIntersect >= 3) {
//			return true;
//		}
//		return false;
	}

}
