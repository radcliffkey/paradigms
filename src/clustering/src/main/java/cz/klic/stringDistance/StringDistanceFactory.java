package cz.klic.stringDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.klic.argparse.ArgParser;
import cz.klic.matrix.SymmetricSparseMatrix;
import cz.klic.stringDistance.matrices.CharDistanceMatrices;
import cz.klic.stringDistance.paradigmDistance.ParadigmDistance;
import cz.klic.util.Fileutil;
import cz.klic.util.StringUtil;

public class StringDistanceFactory {

	public static List<DistanceMetric<String>> getMetric(ArgParser options) throws Exception {
		String metricTypesStr = options.getOptionVal("distanceMeasure");
		List<String> metricTypeStrs = StringUtil.parseList(metricTypesStr);
		List<DistanceMetric<String>> metricList = new ArrayList<DistanceMetric<String>>();
		
		for (String metricTypeStr : metricTypeStrs) {
			DistanceMetric<String> metric = metricFromStr(metricTypeStr, options);
			metricList.add(metric);
		}
		
		if (options.getOptionVal("combination") != null
				&& options.getOptionVal("combination").equals("Euclid")) {
			DistanceMetric<String> combined = new EuclidesCombinedDistance<String>(metricList);
			List<DistanceMetric<String>> result = new ArrayList<DistanceMetric<String>>();
			result.add(combined);
			return result;
		}
		
		return metricList;
	}

	private static StringDistanceMetric metricFromStr(String metricTypesStr, ArgParser options) throws Exception {
		if (metricTypesStr.equals("paradigm")) {
			String wordToPdgmFile = options.getOptionVal("paradigmFile");
			Map<String, Set<String>> wordToParadigms = ParadigmDistance.readWordToParadigmsMap(Fileutil.getBufReader(wordToPdgmFile));
			ParadigmDistance pdgmDist = new ParadigmDistance(wordToParadigms);
			return pdgmDist;
		} else if (metricTypesStr.equals("edit")) {
			SymmetricSparseMatrix distMatrix = CharDistanceMatrices.getCzDiacriticsMatrix();			
			StringDistanceMetric editMetric = new WeightedSufPrefDist(distMatrix);
			return editMetric;
		}
		throw new Exception("Unknown metric type.");
	}
	
}
