package cz.klic.stringDistance.matrices;

import cz.klic.matrix.SymmetricSparseMatrix;

public class CharDistanceMatrices {
	private static char[] vowels = {'a', 'á', 'à', 'e', 'é', 'è', 'ě', 'i', 'í', 'o', 'ó', 'ò', 'u', 'ú', 'ů', 'y', 'ý', 'ü', 'ä', 'ö'};
	
	public static SymmetricSparseMatrix getCzDiacriticsMatrix() {
		SymmetricSparseMatrix distMatrix = new SymmetricSparseMatrix(1.0);
		
		for (char v1 : vowels){
			for (char v2 : vowels){
				if (v1 != v2) {
					distMatrix.set(v1, v2, 0.7);
				}
			}
		}
		
		distMatrix.set('c', 'č', 0.5);
		distMatrix.set('c', 'ç', 0.5);
		distMatrix.set('r', 'ř', 0.5);
		distMatrix.set('s', 'š', 0.5);
		distMatrix.set('z', 'ž', 0.5);
		distMatrix.set('d', 'ď', 0.5);
		distMatrix.set('t', 'ť', 0.5);
		distMatrix.set('n', 'ň', 0.5);
		distMatrix.set('d', 'ď', 0.5);
		distMatrix.set('e', 'ě', 0.5);		
		
		distMatrix.set('e', 'é', 0.5);
		distMatrix.set('e', 'è', 0.5);
		distMatrix.set('a', 'á', 0.5);
		distMatrix.set('a', 'à', 0.5);
		distMatrix.set('a', 'ä', 0.5);
		distMatrix.set('u', 'ú', 0.5);
		distMatrix.set('u', 'ů', 0.5);
		distMatrix.set('u', 'ü', 0.5);
		distMatrix.set('i', 'í', 0.5);
		distMatrix.set('y', 'ý', 0.5);
		distMatrix.set('o', 'ó', 0.5);
		distMatrix.set('o', 'ò', 0.5);
		distMatrix.set('o', 'ö', 0.5);
		
		return distMatrix;
	}
	
	public static SymmetricSparseMatrix getCzPhonChangeMatrix() {
		SymmetricSparseMatrix distMatrix = new SymmetricSparseMatrix(1.0);
		
		for (char v1 : vowels){
			for (char v2 : vowels){
				if (v1 != v2) {
					distMatrix.set(v1, v2, 0.7);
				}
			}
		}
		
		distMatrix.set('k', 'č', 0.5);
		distMatrix.set('k', 'c', 0.4);	
		distMatrix.set('h', 'ž', 0.5);
		distMatrix.set('h', 'z', 0.4);
		distMatrix.set('g', 'ž', 0.5);
		distMatrix.set('g', 'z', 0.4);
		distMatrix.set('d', 'z', 0.4);
		distMatrix.set('r', 'ř', 0.5);
		distMatrix.set('e', 'ě', 0.5);
		
		distMatrix.set('e', 'é', 0.6);
		distMatrix.set('a', 'á', 0.6);
		distMatrix.set('u', 'ú', 0.6);
		distMatrix.set('u', 'ů', 0.6);
		distMatrix.set('i', 'í', 0.6);
		distMatrix.set('y', 'ý', 0.6);
		distMatrix.set('o', 'ó', 0.6);

		return distMatrix;
	}
	
}
