package cz.klic.matrix;

import gnu.trove.map.hash.TIntDoubleHashMap;

public class SymmetricSparseMatrix extends SparseMatrix {

	/**
	 * Create a symmetrical sparse matrix with default value defVal
	 * @param defVal
	 */
	public SymmetricSparseMatrix(double defVal) {
		super(defVal);
	}

	public SymmetricSparseMatrix(double defVal, int initRowCapacity) {
		this(defVal, initRowCapacity, false);
	}
	
	public SymmetricSparseMatrix(double defVal, int initRowCapacity, boolean allocEmptyRows) {
		super(defVal, initRowCapacity);
		if (allocEmptyRows) {
			for (int i = 0; i < initRowCapacity; ++i) {
				this.data.put(i, new TIntDoubleHashMap());
			}
		}
	}

	@Override
	public double get(int row, int col) {
		if (col < row) {
			return super.get(col, row);
		}
		return super.get(row, col);
	}

	@Override
	public void set(int row, int col, double val) {
		if (col < row) {
			super.set(col, row, val);
			return;
		}

		super.set(row, col, val);
	}

}
