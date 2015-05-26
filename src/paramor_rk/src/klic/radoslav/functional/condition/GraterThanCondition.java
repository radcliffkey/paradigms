package klic.radoslav.functional.condition;

public class GraterThanCondition<T extends Comparable<T>> implements Condition<T> {

	private T value;
	
	public GraterThanCondition(T value) {
		super();
		this.value = value;
	}
	
	@Override
	public boolean isTrue(T o1) {
		return (o1.compareTo(this.value) > 0);
	}	

}
