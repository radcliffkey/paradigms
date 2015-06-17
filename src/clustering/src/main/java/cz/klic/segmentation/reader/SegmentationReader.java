package cz.klic.segmentation.reader;

import cz.klic.segmentation.Segmentation;

public interface SegmentationReader<T> {

	public Segmentation<T> readSegmentation() throws Exception;
	
}
