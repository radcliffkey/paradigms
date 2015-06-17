package cz.klic.clustering;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import cz.klic.argparse.ArgParser;
import cz.klic.clustering.HierarchicalClustering.ClusterApproach;
import cz.klic.corpus.Corpus;
import cz.klic.corpus.reader.CorpusReader;
import cz.klic.corpus.reader.PlainCorpusReader;
import cz.klic.functional.condition.RegexMatchCondition;
import cz.klic.functional.transformer.ToDoubleTransformer;
import cz.klic.functional.transformer.ToIntTransformer;
import cz.klic.stringDistance.DistanceMetric;
import cz.klic.stringDistance.StringDistanceFactory;
import cz.klic.util.Fileutil;
import cz.klic.util.StringUtil;
import cz.klic.util.Timer;

/**
 * 
 * Runs hierarchical clustering on corpus word types
 *
 */
public class Clustering {
	
	public static List<Cluster<String>> runClustering(Corpus<String> corpus,
			ArgParser options, boolean verbose) throws Exception {
		List<DistanceMetric<String>> stringMetrics = StringDistanceFactory.getMetric(options);
		int stepCnt = stringMetrics.size();
		
		List<Integer> minClustCnts = new ArrayList<Integer>();
		
		if (options.getOptionVal("minClusterCount") != null) {
			minClustCnts.addAll(StringUtil.parseList((String)options.getOptionVal("minClusterCount"), new ToIntTransformer()));
		}
		//pad the list with 1's
		minClustCnts.addAll(Collections.nCopies(stepCnt - minClustCnts.size(), 1));

		List<Double> distTresholds = new ArrayList<Double>();
		if (options.getOptionVal("distThreshold") != null) {
			distTresholds.addAll(StringUtil.parseList((String)options.getOptionVal("distThreshold"), new ToDoubleTransformer()));
		}
		//pad the list with infinite values ~ no threshold
		distTresholds.addAll(Collections.nCopies(stepCnt - distTresholds.size(), HierarchicalClustering.INF_VAL));
		
		List<Cluster<String>> clusters = HierarchicalClustering.instToClust(corpus.getVocab());
		
		//ClusterApproach clustApproach = ClusterApproach.AVERAGE_DISTANCE;
		List<ClusterApproach> clustTypes = new ArrayList<ClusterApproach>();
		if (options.getOptionVal("clusterApproach") != null) {
            List<ClusterApproach> confClustTypes = StringUtil.parseList(
                    (String) options.getOptionVal("clusterApproach"), ClusterApproach::valueOf);
			clustTypes.addAll(confClustTypes);
		}
		
		clustTypes.addAll(Collections.nCopies(stepCnt - clustTypes.size(), ClusterApproach.AVERAGE_DISTANCE));
		
		for (int i = 0; i < stepCnt; ++i) {				
			DistanceMetric<String> metric = stringMetrics.get(i);
			ClusterApproach clustApproach = clustTypes.get(i);
			
			HierarchicalClustering<String> hc = new HierarchicalClustering<String>(metric, clustApproach);
			hc.setVerbose(verbose);
			Integer threadCount = options.getOptionVal("threadCount");
			if (threadCount != null) {
				hc.setThreadCount(threadCount);
			}
			
			int clustNum = minClustCnts.get(i);
			double threshold = distTresholds.get(i);
			
			System.out.println("Clustering step no. " + (i + 1));
			System.out.println("Clustering approach: " + clustApproach);
			System.out.println("cluster count limit: " + clustNum);
			System.out.println("distance threshold: " + threshold);
			System.out.println("Initial cluster count: " + clusters.size());
			clusters = hc.cluster(clusters, clustNum, threshold);
		}
		
		return clusters;
	}
	
	public static void main(String[] args) {
		try {
			Timer.init();
			
			ArgParser options = new ArgParser();
			
			options.addFlag("verbose", "be verbose", "-v", "--verbose");
			options.addFlag("help", "get a help message", "-h", "--help");
			options.addOption("settingFile", String.class, "setting file", "-f", "--settingFile");
			//currently, only plain text is supported
			//options.addOption("corpusFormat", String.class, "corpus format", "-r","--corpusFormat");
			options.addOption("corpusFile", String.class, "corpus file", "-c", "--corpusFile");
			options.addOption("clusterFile", String.class, "file to store resulting clusters", "-o", "--clusterFile");
			options.addOption("clusterApproach", String.class, "clustering approach", "-a", "--clusterApproach");
			options.addOption("distanceMeasure", String.class, "cluster distance measure", "-m", "--distanceMeasure");
			options.addOption("combination", String.class, "how to combine multiple measures", "--combination");
			options.addOption("paradigmFile", String.class, "paradigm file", "-p", "--paradigmFile");
			options.addOption("minClusterCount", String.class, "minimal cluster count", "-n", "--minClusterCount");
			options.addOption("distThreshold", String.class, "cluster distance threshold", "-t", "--distThreshold");
			options.addOption("threadCount", Integer.class, "number of threads to use", "--threadCount");
						
			options.parseArgs(args);
			
			if (args.length == 0 || (Boolean)options.getOptionVal("help")){
				System.err.println(options.getUsage("EvalClustering"));
				System.exit(1);
			}
			
			String settingFileName = options.getOptionVal("settingFile");
			if (settingFileName != null) {
				Properties settings = new Properties();
				settings.load(Fileutil.getBufReader(settingFileName));
				options.readProperties(settings, false);
			}			
			
			Boolean verbose = options.getOptionVal("verbose");

			String corpusFileName = options.getOptionVal("corpusFile");
			System.out.println("Reading corpus file " + corpusFileName);
			CorpusReader<String> cr = new PlainCorpusReader(corpusFileName);
			Corpus<String> corpus = cr.readCorpus();
			
			System.out.println("Number of types in the corpus: " + corpus.typeCount());
			
			String filterRegex = "[\\D]{4,30}";
			System.out.println("Filtering out words not matching regexp " + filterRegex);
			corpus.filterWords(new RegexMatchCondition(filterRegex));
			System.out.println("Number of filtered types: " + corpus.typeCount());

			List<Cluster<String>> clusters = runClustering(corpus, options, verbose);
			
			for (Cluster<String> cluster : clusters) {
				System.out.println(StringUtil.join("\t", cluster.getMembers()));
			}
			
			String outFileName = options.getOptionVal("clusterFile");
			if (outFileName != null) {
				writeClusters(clusters, outFileName);
				
				System.out.println("Clusters written to " + outFileName);
			}
			
			
			System.out.printf("Elapsed time: %f seconds\n", Timer.elapsedSecs());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeClusters(List<Cluster<String>> clusters,
			String outFileName) throws IOException {
		BufferedWriter outFile = Fileutil.getBufWriter(outFileName);
		for (Cluster<String> cluster : clusters) {
			outFile.write(StringUtil.join("\t", cluster.getMembers()));
			outFile.newLine();
		}
		outFile.close();
	}

}
