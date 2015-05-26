package klic.radoslav.functional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import klic.radoslav.functional.condition.Condition;
import klic.radoslav.functional.transformer.SingleTypeTransformer;
import klic.radoslav.functional.transformer.Transformer;

/**
 * 
 * functions for functional-like programming
 *
 */
public class FuncUtil {

	/**
	 * removes members not satisfying the condition
	 * @param <T>
	 * @param collection
	 * @param condition
	 */
	public static <T> void filter(Collection<T> collection, Condition<? super T> condition) {
		for (Iterator<T> iterator = collection.iterator(); iterator.hasNext();) {
			T obj = iterator.next();
			if (!condition.isTrue(obj)) {
				iterator.remove();
			}
		}
	}
	
	/**
	 * Applies transformation and returns the transformed collection. Original
	 * collection stays unchanged.
	 * 
	 * @param <FromType>
	 * @param <ToType>
	 * @param data - collection to transform
	 * @param transformer
	 * @return transformed data
	 */
	public static <FromType, ToType> List<ToType> transform(
			Iterable<? extends FromType> data,
			Transformer<FromType, ToType> transformer) {
		
		List<ToType> result = new ArrayList<ToType>();
		for (FromType instance : data) {
			result.add(transformer.transform(instance));
		}

		return result;
	}
	
	/**
	 * Tranforms list in-place
	 * @param <T>
	 * @param list
	 * @param transformer
	 */
	@SuppressWarnings("unchecked")
	public static <T> void transform(List<T> list, SingleTypeTransformer<? super T> transformer) {
		for (int i = 0; i < list.size(); i++) {
			list.set(i, (T) transformer.transform(list.get(i)));
		}
	}
	
	/**
	 * Tranforms array in-place
	 * @param <T>
	 * @param list
	 * @param transformer
	 */
	@SuppressWarnings("unchecked")
	public static <T> void transform(T[] data, SingleTypeTransformer<? super T> transformer) {
		for (int i = 0; i < data.length; i++) {
			data[i] = (T) transformer.transform(data[i]);
		}
	}
	
	/**
	 * Applies binary operator to collection members, using result from previous
	 * operation and the next member. Example of use - sum of collection members
	 * 
	 * @param <T>
	 * @param collection
	 * @param operator
	 * @return
	 */
	public static <T> T reduce(Collection<T> collection, BinaryOperator<T> operator) {
		if (collection.isEmpty()) {
			return null;
		}
		
		Iterator<T> iterator = collection.iterator();
		T obj = iterator.next();
		
		while (iterator.hasNext()) {
			T obj2 = iterator.next();
			obj = operator.result(obj, obj2);
		}
		
		return obj;
	}
	
	/**
	 * Applies binary operator to array members, using result from previous
	 * operation and the next member. Example of use - sum of collection members
	 * 
	 * @param <T>
	 * @param collection array to reduce
	 * @param operator
	 * @return
	 */
	public static <T> T reduce(T [] collection, BinaryOperator<T> operator) {
		if (collection.length == 0) {
			return null;
		}
		
		T obj = collection[0];
		
		for (int i = 1; i <  collection.length; ++ i) {
			T obj2 = collection[i];
			obj = operator.result(obj, obj2);
		}
		
		return obj;
	}
	
	/**
	 * Applies binary operator to array members, using result from previous
	 * operation and the next member. Example of use - sum of collection members
	 * 
	 * @param <T>
	 * @param collection array to reduce
	 * @param operator
	 * @return
	 */
	public static double reduce(double [] collection, BinaryOperator<Double> operator) {
		if (collection.length == 0) {
			return Double.MAX_VALUE;
		}
		
		double obj = collection[0];
		
		for (int i = 1; i < collection.length; ++ i) {
			double obj2 = collection[i];
			obj = operator.result(obj, obj2);
		}
		
		return obj;
	}
	
}
