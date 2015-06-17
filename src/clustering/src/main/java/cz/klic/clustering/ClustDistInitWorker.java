package cz.klic.clustering;

import java.util.List;

import gnu.trove.map.TIntDoubleMap;
import cz.klic.clustering.HierarchicalClustering.ClusterDistance;
import cz.klic.matrix.SymmetricSparseMatrix;

/**
 * A thread that computes distances to all other clusters for a given
 * set of clusters
 *
 * @param <T>
 */
public class ClustDistInitWorker<T> extends Thread {

	final private HierarchicalClustering<T> boss;
	final private double distThreshold;

	final private int idMin;
	final private int idMax;

	
	public ClustDistInitWorker(HierarchicalClustering<T> boss, double distThreshold, int idMin, int idMax) {
		this.boss = boss;
		this.distThreshold = distThreshold;
		this.idMin = idMin;
		this.idMax = idMax;
	}

	@Override
	public void run() {
		//System.out.println("tid: " + this.getId() + " startIdx: " + idMin + " endIdx: " + idMax);
		//long startTime = System.nanoTime();
		
		ClusterDistance<T> distComputer = boss.distRecomputer;
		List<Cluster<T>> clusters = boss.clusters;
		int clustCnt = clusters.size();
		SymmetricSparseMatrix distMatrix = boss.distances;
		
		for (int clustId = idMin; clustId < idMax; ++clustId) {
			//System.out.println("tid: " + this.getId() + "cid: " + clustId);
			TIntDoubleMap matrixRow = distMatrix.getRow(clustId);
			matrixRow.put(clustId, 0);
			for (int j = clustId + 1; j < clustCnt; ++j) {
				double dist = distComputer.computeDistance(clusters.get(clustId), clusters.get(j));

				dist = boss.thresholdedDist(dist, distThreshold);
				if (dist != HierarchicalClustering.INF_VAL) {
					matrixRow.put(j, dist);
				}
			}
		}
		//double secs = (System.nanoTime() - startTime) / (double) 1000000000;  
		//System.out.println("tid: " + this.getId() + " finished after " + secs + " seconds");
	}

}
