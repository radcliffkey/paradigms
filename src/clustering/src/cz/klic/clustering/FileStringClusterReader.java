package cz.klic.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cz.klic.util.Fileutil;

public class FileStringClusterReader implements ClusterReader<String> {

	private String fileName;
	private String encoding = "UTF-8";

	public FileStringClusterReader(String fileName) {
		super();
		this.fileName = fileName;
	}

	public FileStringClusterReader(String fileName, String encoding) {
		super();
		this.fileName = fileName;
		this.encoding = encoding;
	}

	@Override
	public List<Cluster<String>> readClusters() throws IOException {
		List<Cluster<String>> clusters = new ArrayList<Cluster<String>>();
		
		Map<String, String> idToMembers = Fileutil.readMap(fileName, encoding);
		for (String membersStr : idToMembers.values()) {
			membersStr = membersStr.trim();
			String[] memArray = membersStr.split("\t");
			Cluster<String> clust = new SimpleCluster<String>(Arrays.asList(memArray));
			clusters.add(clust);
		}
		
		return clusters;
	}
	
	
}
