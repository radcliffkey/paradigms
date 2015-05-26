package cz.klic.clustering;

import java.util.ArrayList;
import java.util.List;

public class SimpleCluster<T> implements Cluster<T> {

	private List<T> members;
	
	public SimpleCluster() {
		this(new ArrayList<T>());
	}

	public SimpleCluster(List<T> members) {
		super();
		this.members = members;
	}

	protected void setMembers(List<T> members) {
		this.members = members;
	}

	@Override
	public List<T> getMembers() {
		return this.members;
	}

	@Override
	public void addMember(T member) {
		this.getMembers().add(member);
	}

	@Override
	public void addMembers(List<T> members) {
		this.getMembers().addAll(members);
	}

	@Override
	public int size() {
		return this.getMembers().size();
	}

}
