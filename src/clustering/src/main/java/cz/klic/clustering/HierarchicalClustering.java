package cz.klic.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.klic.matrix.SymmetricSparseMatrix;
import cz.klic.stringDistance.DistanceMetric;
import cz.klic.util.ListUtil;

/**
 * Hierarchical clustering customisable by distance metric and custering
 * approach
 * 
 * @param <T>
 */
public class HierarchicalClustering<T> {
	
	public static final double INF_VAL = Double.POSITIVE_INFINITY;
	
	/**
	 * Default thread count.
	 */
	private static final int DEF_THREAD_COUNT = 2; 

	/**
	 * measures distance between two objects
	 */
	private DistanceMetric<T> metric;

	/**
	 * distances between clusters
	 */
	SymmetricSparseMatrix distances;

	/**
	 * list of clusters - indices correspond to cluster numbers
	 */
	List<Cluster<T>> clusters;

	/**
	 * defines how cluster distance is computed
	 * NEAREST_MEMBER - cluster distance = distance between the closest members
	 * FURTHEST_MEMBER - cluster distance = distance between the furthest members
	 * AVERAGE_DISTANCE - cluster distance = average distance between members of the 
	 * first and the second cluster
	 */
	public enum ClusterApproach {
		NEAREST_MEMBER, FURTHEST_MEMBER, AVERAGE_DISTANCE
	}

	private ClusterApproach clusterApproach;

	/**
	 * recomputes distance between a newly merged cluster and another cluster
	 */
	ClusterDistance<T> distRecomputer;

	/**
	 * keeps track of which clusters are still active (weren't merged into another one)
	 */
	Set<Integer> validClusters;

	/**
	 * for each cluster no. stores the no. of the closest cluster
	 */
	int[] closestClust;

	/**
	 * first currently merged cluster
	 */
	private int merged1;
	
	/**
	 * second currently merged cluster
	 */
	private int merged2;
	
	private boolean verbose;

	private int threadCount = DEF_THREAD_COUNT;
	
	/**
	 * 
	 * @param metric
	 * @param clusterApproach NEAREST_MEMBER, FURTHEST_MEMBER,...
	 */
	public HierarchicalClustering(DistanceMetric<T> metric,
			ClusterApproach clusterApproach, int threadCount) throws Exception {
		super();
		this.metric = metric;
		
		this.clusterApproach = clusterApproach;
		switch (this.clusterApproach) {
		case NEAREST_MEMBER:
			this.distRecomputer = new NearestMemberDistanceRecomputer(this.metric);
			break;
		case FURTHEST_MEMBER:
			this.distRecomputer = new FurthestMemberDistanceRecomputer(this.metric);
			break;
		case AVERAGE_DISTANCE:
			this.distRecomputer = new AverageDistanceRecomputer(this.metric);
			break;
		default:
			throw new Exception("Unsupported clustering approach");
		}
		
		this.threadCount = threadCount;
	}
	
	public HierarchicalClustering(DistanceMetric<T> metric,
			ClusterApproach clusterApproach) throws Exception {
		this(metric, clusterApproach, DEF_THREAD_COUNT);
	}

	/**
	 * to save space in the dist. matrix, consider distances above a treshold "infinite"
	 * @param dist
	 * @param distThreshold
	 * @return dist or INF_VAL, depending on the threshold and clustering approach
	 */	
	double thresholdedDist(double dist, double distThreshold) {

		if (dist > distThreshold) {
			return INF_VAL;
		}

		return dist;
	}

	/**
	 * Runs hierarchical clustering
	 * @param instances what to cluster
	 * @return resulting clusters
	 * @throws Exception
	 */
	public List<Cluster<T>> cluster(Collection<T> instances) throws Exception {
		return this.cluster(instances, 1, INF_VAL);
	}

	/**
	 * Runs hierarchical clustering
	 * @param instances what to cluster
	 * @param clustNum number of resulting clusters
	 * @return resulting clusters
	 * @throws Exception
	 */
	public List<Cluster<T>> cluster(Collection<T> instances, int clustNum)
			throws Exception {
		return this.cluster(instances, clustNum, INF_VAL);
	}
	
	/**
	 * Runs hierarchical clustering
	 * @param instances what to cluster
	 * @param clustNum number of resulting clusters
	 * @param distThreshold do not merge if the distance is above the threshold
	 * @return resulting clusters
	 * @throws Exception
	 */
	public List<Cluster<T>> cluster(Collection<T> instances, int clustNum,
			double distThreshold) throws Exception {

		return this.cluster(instToClust(instances), clustNum, distThreshold);
	}
	
	/**
	 * Runs hierarchical clustering
	 * @param instances what to cluster
	 * @param clustNum number of resulting clusters
	 * @param distThreshold do not merge if the distance is above the threshold
	 * @return resulting clusters
	 * @throws Exception
	 */
	public List<Cluster<T>> cluster(List<Cluster<T>> clusters, int clustNum,
			double distThreshold) throws Exception {

		this.clusters = new ArrayList<Cluster<T>>(clusters);
		this.initDataStructs(distThreshold);

		while (this.validClusters.size() > clustNum) {
			if (this.isVerbose()) {
				System.out.println("merge dist: " + this.distances.get(this.merged1, this.merged2));
			}
			
			if (this.distances.get(this.merged1, this.merged2) > distThreshold) {
				break;
			}

			Cluster<T> mergedCluster1 = this.clusters.get(this.merged1);
			Cluster<T> mergedCluster2 = this.clusters.get(this.merged2);

			if (this.isVerbose()) {
				System.out.println("Merging:");
				System.out.println(mergedCluster1.getMembers());
				System.out.println(mergedCluster2.getMembers());
			}

			this.validClusters.remove(this.merged2);
			this.clusters.set(merged2, null);
			this.distances.set(merged1, merged2, INF_VAL);

			int newMerged1 = -1;
			int newMerged2 = -1;
			double globalmin = INF_VAL;
			double mergedClosestDist = INF_VAL;

			/**
			 * for each cluster, recompute the distance to newly merged cluster
			 * and update the no. of the closest cluster
			 */
			for (Integer i : this.validClusters) {
				if (i != this.merged1) {

					double dist1 = this.distances.get(i, merged1);
					double dist2 = this.distances.get(i, merged2);

					Cluster<T> c = this.clusters.get(i);
					double newDist = this.distRecomputer.recomputeDistance(
							mergedCluster1, mergedCluster2, c, dist1, dist2);

					newDist = this.thresholdedDist(newDist, distThreshold);
					this.distances.set(i, merged1, newDist);

					if (newDist < mergedClosestDist) {
						this.closestClust[merged1] = i;
						mergedClosestDist = newDist;
					}

					double distToClosest = this.distances.get(i,
							this.closestClust[i]);

					if (newDist < distToClosest) {
						this.closestClust[i] = merged1;
					}

					if (this.closestClust[i] == merged2) {
						this.closestClust[i] = merged1;
					}

					if (this.closestClust[i] == merged1
							&& newDist > distToClosest) {
						updateClosest(i);
					}

					distToClosest = this.distances.get(i, this.closestClust[i]);
					if (distToClosest < globalmin) {
						globalmin = distToClosest;
						newMerged1 = Math.min(i, this.closestClust[i]);
						newMerged2 = Math.max(i, this.closestClust[i]);
					}
				}
			}

			mergedCluster1.addMembers(mergedCluster2.getMembers());
			this.merged1 = newMerged1;
			this.merged2 = newMerged2;
		}

		ListUtil.retainAll(this.clusters, this.validClusters);

		return this.clusters;
	}

	/**
	 * Find the closest cluster to the i-th cluster by iterating through all
	 * other clusters
	 * 
	 * @param i
	 */
	private void updateClosest(Integer i) {
		double dist = INF_VAL;
		double min = INF_VAL;
		for (Integer j : this.validClusters) {
			if (j != i) {
				try {
					dist = this.distances.get(i, j);
				} catch (Exception e) {
					// This should not happen
					e.printStackTrace();
				}
				if (dist < min) {
					min = dist;
					this.closestClust[i] = j;
				}
			}
		}
	}
	
	/**
	 * Creates single-membered clusters from given instances
	 * @param instances
	 * @return list of single-membered clusters
	 */
	public static <T> List<Cluster<T>> instToClust(Collection<T> instances) {
		int size = instances.size();

		ArrayList<Cluster<T>> clusters = new ArrayList<Cluster<T>>(size);

		for (T t : instances) {
			Cluster<T> cluster = new SimpleCluster<T>();
			cluster.addMember(t);
			clusters.add(cluster);
		}
		return clusters;
	}

	/**
	 * Initialise the data structures - create a cluster for each instance and
	 * compute distances between clusters
	 * 
	 * @param instances
	 * @param distThreshold
	 * @throws Exception
	 */
	private void initDataStructs(Double distThreshold)
			throws Exception {
		int size = this.clusters.size();

		this.validClusters = new HashSet<Integer>(size);
		for (int clustId = 0; clustId < size; ++clustId) {
			this.validClusters.add(clustId);
		}
		
		this.distances = new SymmetricSparseMatrix(INF_VAL, size, true);

		/*
		 * Divide the work of computing distances between all clusters
		 * into more threads. Each thread recieves a range of cluster IDs
		 * to process
		 */
		@SuppressWarnings("unchecked")
		ClustDistInitWorker<T> workers[] = new ClustDistInitWorker[threadCount];
		
		if (this.threadCount > 1) {
			List<Integer> intervals = this.divideInitWork(size, this.threadCount);
	
			for (int i = 0; i < threadCount; ++i) {
				int min = intervals.get(i);
				int max = intervals.get(i + 1);
				workers[i] =  new ClustDistInitWorker<T>(this, distThreshold, min, max);
			}
		} else {
			workers[0] = new ClustDistInitWorker<T>(this, distThreshold, 0, size);
		}
		
		for (ClustDistInitWorker<T> worker : workers) {
			worker.start();
		}
		
		for (ClustDistInitWorker<T> worker : workers) {
			worker.join();
		}

		this.initClosest();

	}

	/**
	 * Divides rows of an upper triangular matrix to intervals
	 * containing roughly the same number of non-zero values 
	 * @param size number of rows
	 * @param workerCnt number of intervals
	 * @return list of boundary points of the intervals
	 */
	private List<Integer> divideInitWork(int size, int workerCnt) {
		List<Integer> result = new ArrayList<Integer>(workerCnt + 1);
		
		int totalWork = (size * (size - 1)) / 2;
		
		double workForOne = totalWork / (double) workerCnt;
		
		result.add(0);
		int prevWork = 0;
		int currWork = 0;
		
		for (int i = 0; i < size; ++ i) {
			int stepWork = size - i - 1;
			currWork = prevWork + stepWork;
			if (currWork > workForOne) {
				result.add(i + 1);
				currWork = 0;
				if (result.size() == workerCnt) {
					result.add(size);
					break;
				}
			}
			
			prevWork = currWork;
		}
		return result;
	}

	private void initClosest() throws Exception {
		int size = this.validClusters.size();
		this.closestClust = new int[size];
	
		@SuppressWarnings("unchecked")
		ClustClosestFindWorker<T> workers[] = new ClustClosestFindWorker[threadCount];
		
		/*
		 * Divide the work of computing the closest cluster
		 * between more threads. Each thread recieves an interval
		 * of cluster IDs to process.
		 */
		int step = size / this.threadCount;
		int min = 0;
		int max = step;
		int i = 0;
		for (; i < threadCount - 1; ++i) {
			workers[i] =  new ClustClosestFindWorker<T>(this, min, max);
			min = max;
			max += step;
		}
		workers[i] =  new ClustClosestFindWorker<T>(this, min, size);
		
		for (ClustClosestFindWorker<T> worker : workers) {
			worker.start();
		}
		
		for (ClustClosestFindWorker<T> worker : workers) {
			worker.join();
		}
		
		double globalMin = INF_VAL;
		
		for (i = 0; i < this.closestClust.length; ++i) {
			int j = this.closestClust[i];
			double dist = this.distances.get(i, j);
			
			if (dist < globalMin) {
				globalMin = dist;
				if (i < j) {
					this.merged1 = i;
					this.merged2 = j;
				} else {
					this.merged1 = j;
					this.merged2 = i;
				}
			}
		}
	
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	/**
	 * 
	 * recomputes distance between a newly merged cluster and another cluster
	 *
	 * @param <T>
	 */
	protected interface ClusterDistance<T> {
		public double recomputeDistance(Cluster<T> merged1, Cluster<T> merged2,
				Cluster<T> otherCluster, double dist1, double dist2);
		public double computeDistance(Cluster<T> cluster1, Cluster<T> cluster2);
	}

	protected abstract class AbstractDistanceRecomputer implements ClusterDistance<T> {

		protected DistanceMetric<T> memberDistanceMetric;

		public AbstractDistanceRecomputer(DistanceMetric<T> metric) {
			this.memberDistanceMetric = metric;
		}
		
		@Override
		public abstract double recomputeDistance(Cluster<T> merged1, Cluster<T> merged2,
				Cluster<T> otherCluster, double dist1, double dist2);

		@Override
		public abstract double computeDistance(Cluster<T> cluster1, Cluster<T> cluster2);
	}
	
	protected class NearestMemberDistanceRecomputer extends
		AbstractDistanceRecomputer {

		public NearestMemberDistanceRecomputer(
				DistanceMetric<T> metric) {
			super(metric);
		}

		@Override
		public double recomputeDistance(Cluster<T> merged1, Cluster<T> merged2,
				Cluster<T> otherCluster, double dist1, double dist2) {
			return Math.min(dist1, dist2);
		}

		@Override
		public double computeDistance(Cluster<T> cluster1, Cluster<T> cluster2) {
			double min = Double.MAX_VALUE;
			for (T member1  : cluster1.getMembers()){
				for (T member2  : cluster2.getMembers()){
					min = Math.min(min, this.memberDistanceMetric.getDistance(member1, member2));
				}
			}
			
			return min;
		}

	}

	protected class FurthestMemberDistanceRecomputer extends
		AbstractDistanceRecomputer {

		public FurthestMemberDistanceRecomputer(
				DistanceMetric<T> metric) {
			super(metric);
		}

		@Override
		public double recomputeDistance(Cluster<T> merged1, Cluster<T> merged2,
				Cluster<T> otherCluster, double dist1, double dist2) {
			return Math.max(dist1, dist2);
		}

		@Override
		public double computeDistance(Cluster<T> cluster1, Cluster<T> cluster2) {
			double max = Double.MIN_VALUE;
			for (T member1  : cluster1.getMembers()){
				for (T member2  : cluster2.getMembers()){
					max = Math.max(max, this.memberDistanceMetric.getDistance(member1, member2));
				}
			}
			
			return max;
		}

	}

	protected class AverageDistanceRecomputer extends
			AbstractDistanceRecomputer {
		
		public AverageDistanceRecomputer(
				DistanceMetric<T> metric) {
			super(metric);

		}

		@Override
		public double recomputeDistance(Cluster<T> merged1, Cluster<T> merged2,
				Cluster<T> otherCluster, double dist1, double dist2) {
			if (dist1 == INF_VAL && dist2 == INF_VAL) {
				return INF_VAL; 
			}
			
			if (dist1 == INF_VAL) {
				dist1 = this.computeDistance(merged1, otherCluster); 
			}
			
			if (dist2 == INF_VAL) {
				dist2 = this.computeDistance(merged2, otherCluster); 
			}
			
			int size1 = merged1.size();
			int size2 = merged2.size();
			double result = (size1 * dist1 + size2 * dist2) / (size1 + size2);
			return result;
		}

		public double computeDistance(Cluster<T> cluster1, Cluster<T> cluster2) {
			double sum = 0;
			int cnt = 0;
			for (T member1  : cluster1.getMembers()) {
				for (T member2  : cluster2.getMembers()) {
					sum += this.memberDistanceMetric.getDistance(member1, member2);
					++cnt;
				}
			}
			
			return sum / cnt;
		}

	}
}
