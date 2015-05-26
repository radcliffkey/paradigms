package cz.klic.clustering;

import java.util.List;

public interface ClusterReader<MemberType> {

	List<Cluster<MemberType>> readClusters() throws Exception;
}
