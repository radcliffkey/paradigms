package klic.radoslav.functional.condition;

import java.util.function.Predicate;

public class GraterThanCondition<T extends Comparable<T>> implements Predicate<T> {

	private T value;
	
	public GraterThanCondition(T value) {
		super();
		this.value = value;
	}
	
	@Override
	public boolean test(T o1) {
		return (o1.compareTo(this.value) > 0);
	}	

}
