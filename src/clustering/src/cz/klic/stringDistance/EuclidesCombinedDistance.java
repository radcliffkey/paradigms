package cz.klic.stringDistance;

import java.util.List;

public class EuclidesCombinedDistance<T> extends AbstractCombinedDistance<T> {

	public EuclidesCombinedDistance(List<? extends DistanceMetric<T>> metrics) {
		super(metrics);
	}

	@Override
	public double getDistance(T o1, T o2) {
		double sum = 0;
		for (DistanceMetric<T> metric : this.metrics) {
			double dist = metric.getDistance(o1, o2);
			sum += dist * dist;
		}
		
		double result = Math.sqrt(sum);
		return result;
	}

}
