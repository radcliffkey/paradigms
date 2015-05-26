package cz.klic.test;

import java.io.IOException;

import cz.klic.matrix.SymmetricSparseMatrix;
import cz.klic.segmentation.StringSegmentation;
import cz.klic.segmentation.reader.ParamorSegmentationReader;
import cz.klic.stringDistance.MorphBoundEditDistance;
import cz.klic.stringDistance.matrices.CharDistanceMatrices;

public class TestMorphBoundDistance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String segmentedFileName = "../resources/segmentedDevSample.txt";
			
			ParamorSegmentationReader reader = new ParamorSegmentationReader(segmentedFileName);
			StringSegmentation segmentation = reader.readSegmentation();
			
			SymmetricSparseMatrix distMatrix = CharDistanceMatrices.getCzDiacriticsMatrix();
			
			MorphBoundEditDistance distMetric = new MorphBoundEditDistance(distMatrix, segmentation);
			double dist = distMetric.getDistance("ministr", "minisys");
			System.out.println(dist);
			
			dist = distMetric.getDistance("věkového", "nového");
			System.out.println(dist);
			
		}  catch (Exception e) {
			e.printStackTrace();
		}

	}

}
