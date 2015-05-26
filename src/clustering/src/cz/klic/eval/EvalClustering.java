package cz.klic.eval;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cz.klic.argparse.ArgParser;
import cz.klic.clustering.Cluster;
import cz.klic.clustering.Clustering;
import cz.klic.clustering.FileStringClusterReader;
import cz.klic.clustering.SimpleCluster;
import cz.klic.corpus.CorpusFactory;
import cz.klic.corpus.CorpusFactory.TaggedFormat;
import cz.klic.corpus.TaggedCorpus;
import cz.klic.corpus.TaggedWord;
import cz.klic.functional.condition.RegexMatchCondition;
import cz.klic.functional.transformer.LowerCaser;
import cz.klic.functional.transformer.SingleTypeTransformer;
import cz.klic.matrix.SymmetricSparseMatrix;
import cz.klic.util.Fileutil;
import cz.klic.util.ListUtil;
import cz.klic.util.StringUtil;
import cz.klic.util.Timer;

public class EvalClustering {
	
	public static double fMeasure(double precision, double recall) {
		double f = (2 * precision * recall) / (precision + recall);
		return f;
	}
	
	public static class EvalResult {
		public EvalResult(double precision, double recall) {
			super();
			this.precision = precision;
			this.recall = recall;
		}
		public double precision;
		public double recall;
		@Override
		public String toString() {
			double f = fMeasure(precision, recall);
			return String.format("[precision = %.4f, recall = %.4f, f-measure: %.4f]", precision, recall, f);
		}
	}
	
	public static EvalResult evalClustersGraph(List<Cluster<String>> clusters, Collection<Set<String>> formSets,
			boolean multiGraph, boolean verbose) {
		
		Map<String, Integer> wordToId = new HashMap<String, Integer>();
		int currId = 0;
		
		for (Cluster<String> clust : clusters) {
			for (String word : clust.getMembers()) {
				if (!wordToId.containsKey(word)) {
					wordToId.put(word, currId);
					++currId;
				}
			}
		}
		
		for (Set<String> formSet : formSets) {
			for (String word : formSet) {
				if (!wordToId.containsKey(word)) {
					wordToId.put(word, currId);
					++currId;
				}
			}
		}
		
		SymmetricSparseMatrix trueGraph = new SymmetricSparseMatrix(0);
		SymmetricSparseMatrix predGraph = new SymmetricSparseMatrix(0);
		
		for (Set<String> formSet : formSets) {
			if (formSet.size() <= 1) {
				continue;
			}
			
			List<String> words = new ArrayList<String>(formSet);
			for (int i = 0; i < words.size(); ++i) {
				int id1 = wordToId.get(words.get(i));
				for(int j = i + 1; j < words.size(); ++j) {
					int id2 = wordToId.get(words.get(j));
					if (multiGraph) {
						trueGraph.set(id1, id2, trueGraph.get(id1, id2) + 1);
					} else {
						trueGraph.set(id1, id2, 1);
					}
				}
			}			
		}
		
		for (Cluster<String> clust : clusters) {
			if (clust.size() <= 1) {
				continue;
			}		

			List<String> words = clust.getMembers();
			for (int i = 0; i < words.size(); ++i) {
				int id1 = wordToId.get(words.get(i));
				for(int j = i + 1; j < words.size(); ++j) {
					int id2 = wordToId.get(words.get(j));
					if (multiGraph) {
						predGraph.set(id1, id2, predGraph.get(id1, id2) + 1);
					} else {
						predGraph.set(id1, id2, 1);
					}
				}
			}
		}
		
		Integer tp = 0;
		int fp = 0;
		int fn = 0;
		
		TIntObjectHashMap<TIntDoubleMap> trueGrpEdges = trueGraph.getRows();

		TIntObjectIterator<TIntDoubleMap> it1 = trueGrpEdges.iterator();
		while (it1.hasNext()) {
			it1.advance();
			int vertex1 = it1.key();
			TIntDoubleIterator it2 = it1.value().iterator();
			while (it2.hasNext()) {
				it2.advance();
				int vertex2 = it2.key();
				int trueEdges = (int) it2.value();
				int predEdges = (int) predGraph.get(vertex1, vertex2);
				
				int localTp = Math.min(trueEdges, predEdges);
				tp += localTp;
				
				if (trueEdges > predEdges) {
					fn += trueEdges - localTp;
				}				
			}
		}
		
		if (verbose) {
			System.out.println("TP: " + tp);
			System.out.println("FN: " + fn);
		}
		tp = 0;
		
		TIntObjectHashMap<TIntDoubleMap> predGrpEdges = predGraph.getRows();

		it1 = predGrpEdges.iterator();
		while (it1.hasNext()) {
			it1.advance();
			int vertex1 = it1.key();
			TIntDoubleIterator it2 = it1.value().iterator();
			while (it2.hasNext()) {
				it2.advance();
				int vertex2 = it2.key();
				int predEdges = (int) it2.value();
				int trueEdges = (int) trueGraph.get(vertex1, vertex2);
				
				int localTp = Math.min(trueEdges, predEdges);
				tp += localTp;
				
				if (predEdges > trueEdges) {
					fp += predEdges - localTp;
				}
			}
		}
		
		if (verbose) {
			System.out.println("TP: " + tp);
			System.out.println("FP: " + fp);
		}
		
		double precision = (double)tp / (tp + fp);
		double recall = (double)tp / (tp + fn);
		
		return new EvalResult(precision, recall);
	}
	
	public static EvalResult evalClusters(List<Cluster<String>> clusters, Collection<Set<String>> formSets, boolean verbose) {
		int correct = 0;
		int clustSizeSum = 0;
		int formSetSizeSum = 0;
		
		for (Set<String> formSet : formSets) {
			Cluster<String> closestCluster = findClosestCluster(formSet, clusters);
			List<String> clustMembers = closestCluster.getMembers();
			
			Set<String> intesection = new HashSet<String>(formSet);
			intesection.retainAll(clustMembers);
			
			correct += intesection.size();			
			formSetSizeSum += formSet.size();
		}
		
		double recall = correct / (double) formSetSizeSum;
		
		correct = 0;
		for (Cluster<String> cluster : clusters) {
			List<String> clustMembers = cluster.getMembers();
			Set<String> clustMemberSet = new HashSet<String>(clustMembers);
			Set<String> closestFormSet = findClosestFormset(cluster, formSets);
			
			Set<String> intesection = new HashSet<String>(closestFormSet);
			intesection.retainAll(clustMemberSet);
			
			Set<String> missed = new HashSet<String>(closestFormSet);
			missed.removeAll(intesection);
			
			Set<String> wrong = new HashSet<String>(clustMemberSet);
			wrong.removeAll(intesection);
			
			if (verbose) {
				System.out.println("cluster:" + StringUtil.join(", ", ListUtil.sortedSet(clustMemberSet)));
				System.out.println("correct:" + StringUtil.join(", ", ListUtil.sortedSet(intesection)));
				System.out.println("wrong:" + StringUtil.join(", ", ListUtil.sortedSet(wrong)));
				System.out.println("missed:" + StringUtil.join(", ", ListUtil.sortedSet(missed)));
				System.out.println();
			}
			
			correct += intesection.size();
			clustSizeSum += clustMembers.size();
		}
		
		double precision = correct / (double) clustSizeSum;
		
		if (verbose) {
			System.out.println("Cluster size sum: " + clustSizeSum);
			System.out.println("Form set size sum: " + formSetSizeSum);
		}
		
		return new EvalResult(precision, recall);
	}

	private static Cluster<String> findClosestCluster(Set<String> formSet,
			List<Cluster<String>> clusters) {
		Cluster<String> closest = null;
		int bestIntersectSize = -1;
		for (Cluster<String> cluster : clusters) {
			Set<String> intesection = new HashSet<String>(formSet);
			intesection.retainAll(cluster.getMembers());
			int intersectSize = intesection.size();
			if (intersectSize > bestIntersectSize) {
				bestIntersectSize = intersectSize;
				closest = cluster;
			}
		}
		return closest;
	}

	private static Set<String> findClosestFormset(Cluster<String> cluster,
			Collection<Set<String>> formSets) {
		Set<String> closest = null;
		int bestIntersectSize = -1;
		
		for (Set<String> formSet : formSets) {
			Set<String> intesection = new HashSet<String>(formSet);
			intesection.retainAll(cluster.getMembers());
			int intersectSize = intesection.size();
			if (intersectSize > bestIntersectSize) {
				bestIntersectSize = intersectSize;
				closest = formSet;
			}
		}
		
		return closest;		
	}

	public static void main(String[] args) {
		try {
			Timer.init();
			
			ArgParser options = new ArgParser();
			
			options.addFlag("verbose", "be verbose", "-v", "--verbose");
			options.addFlag("help", "get a help message", "-h", "--help");
			options.addFlag("graphOnly", "graph style evaluation only", "-g", "--graphOnly");
			options.addOption("settingFile", String.class, "setting file", "-f", "--settingFile");
			options.addOption("corpusFormat", String.class, "corpus format", "-r","--corpusFormat");
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

			TaggedFormat corpusFormat = TaggedFormat.valueOf((String)options.getOptionVal("corpusFormat"));
			
			TaggedCorpus<String, String, String> corpus = CorpusFactory.getCorpus(corpusFileName, corpusFormat);
			
			corpus.transformWords(new LowerCaser());
			
			if (corpusFormat.equals(TaggedFormat.TIGER_EXPORT)) {
				corpus.transform(new SingleTypeTransformer<TaggedWord<String,String,String>>() {
					
					@Override
					public TaggedWord<String, String, String> transform(
							TaggedWord<String, String, String> wordStruct) {
						if (wordStruct.getLemma().equals("--")) {
							wordStruct.setLemma(wordStruct.getForm());
						}
						return wordStruct;
					}
				});
			}
			
			System.out.println("Number of types in the corpus: " + corpus.typeCount());
			System.out.println("Number of lemmas in the corpus: " + corpus.lemmaCount());
			
			String filterRegex = "[\\D]{4,30}";
			System.out.println("Filtering out words not matching regexp " + filterRegex);
			corpus.filterWords(new RegexMatchCondition(filterRegex));
			System.out.println("Number of filtered types: " + corpus.typeCount());
			
			Map<String, Set<String>> formSetMap = corpus.getLemmaToFormsMap();
			Collection<Set<String>> formSets = formSetMap.values();
			System.out.println("True formset count: " + formSets.size());
			
//			String segmentedFileName = args[1];
//			String stemVariantFileName = args[2];
			
			//System.out.println("Reading segmentation file " + segmentedFileName);
//			ParamorSegmentationReader reader = new ParamorSegmentationReader(segmentedFileName);
//			StringSegmentation segmentation = reader.readSegmentation();
			
			List<Cluster<String>> clusters;
			
			if (options.getOptionVal("clusterApproach") != null
					&& options.getOptionVal("clusterApproach").equals("NO_CLUSTERING")) {
				String pdgmFileName = options.getOptionVal("paradigmFile");
				FileStringClusterReader clustReader = new FileStringClusterReader(pdgmFileName, "UTF-8");
				clusters = clustReader.readClusters();
				addSingleWords(clusters, corpus);
			} else {
				Timer.init();
				clusters = Clustering.runClustering(corpus, options, verbose);
				//double clustTime = Timer.elapsedSecs();
				//System.out.println("Clustering time: " + clustTime);
			}			
			
//			System.out.println("Reading stem variant file " + stemVariantFileName);
//			StemVariants stemVariants = StemVariants.read(Fileutil.getBufReader(stemVariantFileName));
			
			System.out.println("candidate formset count: " + clusters.size());
			
			Boolean graphOnly = options.getOptionVal("graphOnly");
			
			EvalResult evalResult = null;
			
			if (!graphOnly) {
				evalResult = evalClusters(clusters, formSets, verbose);
				System.out.println("Cluster matching evaluation: " + evalResult);
			}
			
			evalResult = evalClustersGraph(clusters, formSets, false, verbose);			
			System.out.println("Graph evaluation: " + evalResult);

			String outFileName = options.getOptionVal("clusterFile");
			if (outFileName != null) {
				Clustering.writeClusters(clusters, outFileName);
				
				System.out.println("Clusters written to " + outFileName);
			}
			
			System.out.printf("Elapsed time: %f seconds\n", Timer.elapsedSecs());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addSingleWords(List<Cluster<String>> clusters,
			TaggedCorpus<String, String, String> corpus) {

		Set<String> remainingWords = new HashSet<String>(corpus.getVocab());
		for (Cluster<String> clust : clusters) {
			for (String member : clust.getMembers()) {
				remainingWords.remove(member);
			}
		}
		
		for (String word : remainingWords) {
			Cluster<String> clust = new SimpleCluster<String>();
			clust.addMember(word);
			clusters.add(clust);
		}
	}
	
}
