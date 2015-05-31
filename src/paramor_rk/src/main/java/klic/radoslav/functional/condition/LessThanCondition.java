package klic.radoslav.functional.condition;

import java.util.function.Predicate;

public class LessThanCondition<T extends Comparable<T>> implements Predicate<T> {

	private T value;
	
	public LessThanCondition(T value) {
		super();
		this.value = value;
	}
	
	@Override
	public boolean test(T o1) {
		return (o1.compareTo(this.value) < 0);
	}	

}
