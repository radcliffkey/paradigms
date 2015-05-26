package cz.klic.matrix;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SparseMatrix {
	
	private double defVal;

	protected TIntObjectHashMap<TIntDoubleMap> data;
	
	/**
	 * Create a sparse matrix with default value defVal
	 * @param defVal
	 */
	public SparseMatrix(double defVal) {
		this.defVal = defVal;
		this.data = new TIntObjectHashMap<TIntDoubleMap>();
	}
	
	public SparseMatrix(double defVal, int initRowCapacity) {
		this.defVal = defVal;
		this.data = new TIntObjectHashMap<TIntDoubleMap>(initRowCapacity);
	}

	public TIntDoubleMap getRow(int row) {
		return this.data.get(row);
	}
	
	public double get(int row, int col) {
		
		TIntDoubleMap rowMap = this.data.get(row);
		if (rowMap == null) {
			return this.defVal;
		}
		if (!rowMap.containsKey(col)) {
			return this.defVal;
		}
		
		double result = rowMap.get(col);
		return result;
	}
	
	public void set(int row, int col, double val) {
		TIntDoubleMap rowMap = this.data.get(row);
		
		if (val == this.defVal) {
			if (rowMap != null) {
				rowMap.remove(col);
			}
			return;
		}
		
		if (rowMap == null) {
			rowMap = new TIntDoubleHashMap();
			rowMap.put(col, val);
			this.data.put(row, rowMap);
		} else {
			rowMap.put(col, val);
		}
	}
	
	public TIntObjectHashMap<TIntDoubleMap> getRows() {
		return this.data;
	}
}
