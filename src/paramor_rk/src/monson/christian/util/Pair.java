package monson.christian.util;

public class Pair<L, R> {

	protected L left = null;
	protected R right = null;

	public Pair() {
	}

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}

	@Override
	public int hashCode() {
		int hashCode = left.hashCode() + right.hashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		Pair<L, R> that = (Pair<L, R>) o;
		if (!this.left.equals(that.left)) {
			return false;
		}

		if (!this.right.equals(that.right)) {
			return false;
		}

		return true;
	}

}
