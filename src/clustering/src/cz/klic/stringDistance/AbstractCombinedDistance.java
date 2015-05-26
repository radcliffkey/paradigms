package cz.klic.stringDistance;

import java.util.List;

public abstract class AbstractCombinedDistance<T> implements DistanceMetric<T> {

	protected List <? extends DistanceMetric<T>> metrics;
	
	public AbstractCombinedDistance(List<? extends DistanceMetric<T>> metrics) {
		super();
		this.metrics = metrics;
	}

	@Override
	public abstract double getDistance(T o1, T o2);

}
