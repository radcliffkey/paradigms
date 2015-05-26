package cz.klic.clustering;

import java.util.List;

/**
 * Represents a cluster of objects to be used in a clustering algorithm
 * 
 * @param <T>
 */
public interface Cluster<T> {

	public List<T> getMembers();
	
	public void addMember(T member);
	
	public void addMembers(List<T> members);
	
	public int size();
	
}
