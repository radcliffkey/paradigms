package cz.klic.clustering;

import cz.klic.matrix.SymmetricSparseMatrix;

/**
 * A thread that computes the closest cluster for each cluster from a given
 * set.
 *
 * @param <T>
 */
public class ClustClosestFindWorker<T> extends Thread {

	final private HierarchicalClustering<T> boss;
	final private int idMin;
	final private int idMax;
	
	public ClustClosestFindWorker(HierarchicalClustering<T> boss,  int idMin, int idMax) {
		this.boss = boss;
		
		this.idMin = idMin;
		this.idMax = idMax;
	}

	@Override
	public void run() {
		//System.out.println("tid: " + this.getId() + " startIdx: " + idMin + " endIdx: " + idMax);
		
		SymmetricSparseMatrix distMatrix = this.boss.distances;
		
		for (int clustId = idMin; clustId < idMax; ++clustId) {
			//System.out.println("tid: " + this.getId() + "cid: " + clustId);
			double min = HierarchicalClustering.INF_VAL;
			double dist = HierarchicalClustering.INF_VAL;
			for (int j = 0; j < clustId; ++j) {
				try {
					dist = distMatrix.get(j, clustId);
				} catch (Exception e) {
					// This should not happen
					e.printStackTrace();
				}
				if (dist < min) {
					min = dist;
					boss.closestClust[clustId] = j;
				}
			}

			for (int j = clustId + 1; j < boss.validClusters.size(); ++j) {
				try {
					dist = distMatrix.get(clustId, j);
				} catch (Exception e) {
					// This should not happen
					e.printStackTrace();
				}
				if (dist < min) {
					min = dist;
					boss.closestClust[clustId] = j;
				}
			}
		}			
	}

}
