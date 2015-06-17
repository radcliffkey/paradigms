package cz.klic.segmentation;

/**
 * 
 * represents segmentation of tokens into smaller tokens - e.g. strings into
 * morphs
 * 
 * @param <T>
 */
public interface Segmentation<T> {

	public T[] getSegments(T token);

}
