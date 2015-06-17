package cz.klic.segmentation;

public interface StringSegmentation extends Segmentation<String> {
	
	/**
	 * 
	 * @return cumulative lengths of segments
	 */
	public int[] cumLengths(String s);

}
