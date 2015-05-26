package cz.klic.stringDistance;

public interface DistanceMetric<T> {

	public double getDistance(T o1, T o2);
}
