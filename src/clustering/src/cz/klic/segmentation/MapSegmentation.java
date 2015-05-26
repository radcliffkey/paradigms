package cz.klic.segmentation;

import java.util.Map;

public class MapSegmentation<T> implements Segmentation<T> {

	private Map<T, T[]> segmentMap;
	
	public MapSegmentation(Map<T, T[]> segmentMap) {
		super();
		this.segmentMap = segmentMap;
	}

	public Map<T, T[]> getSegmentMap() {
		return segmentMap;
	}

	@Override
	public T[] getSegments(T token) {
		return this.segmentMap.get(token);
	}
}
