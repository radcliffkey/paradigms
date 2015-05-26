package cz.klic.matrix;

/**
 * 
 * Symmetrical matrix - m[i][j] equals m[j][i]
 *
 */
public class SymmetricMatrix {
	
	private int dim;
	
	private double[][] data;
	
	public SymmetricMatrix(int dim) {
		super();
		this.dim = dim;
		
		this.data = new double[dim][];
		for (int i = 0; i < dim; i++) {
			this.data[i] = new double[dim - i];
		}
	}

	public int getDim() {
		return this.dim;
	}

	protected void setDim(int dim) {
		this.dim = dim;
	}

	protected double[][] getData() {
		return data;
	}

	protected void setData(double[][] data) {
		this.data = data;
	}
	
	public double get(int row, int col) throws Exception {
		double result = 0;
		int x;
		int y;
		
		if (row > col) {
			x = col;
			y = row;
		} else {
			x = row;
			y = col;
		}
		
		try {
			result = this.data[x][y - x];
		} catch (Exception e) {
			throw new Exception(String.format("Cordinates %d,%d are out of the matrix", row, col));							
		}
		return result;
	}

	public void set(int row, int col, double val) throws Exception {
		
		int x;
		int y;
		
		if (row > col) {
			x = col;
			y = row;
		} else {
			x = row;
			y = col;
		}
		
		try {
			this.data[x][y - x] = val;
		} catch (Exception e) {
			throw new Exception(String.format("Cordinates %d,%d are out of the matrix", row, col));				
		}
	}
}
