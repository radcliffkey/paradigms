package cz.klic.test;

import cz.klic.matrix.SymmetricMatrix;

public class TestMatrix {

	public static void main(String[] args) throws Exception {
		SymmetricMatrix m = new SymmetricMatrix(3);
		
		int val = 0;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				m.set(i, j, val);
				System.out.println(m.get(i, j));
				++val;
			}
		}
		
		try {
			m.set(2, 0, 6);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println(m.get(2, 0));
		
		try {
			m.set(2, 5, 7);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			System.out.println(m.get(2, 5));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
