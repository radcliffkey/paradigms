package klic.radoslav.functional;

@FunctionalInterface
public interface BinaryOperator<T> {

	public T result(T obj1, T obj2);
	
}
