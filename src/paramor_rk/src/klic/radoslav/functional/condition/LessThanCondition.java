package klic.radoslav.functional.condition;

public class LessThanCondition<T extends Comparable<T>> implements Condition<T> {

	private T value;
	
	public LessThanCondition(T value) {
		super();
		this.value = value;
	}
	
	@Override
	public boolean isTrue(T o1) {
		return (o1.compareTo(this.value) < 0);
	}	

}
