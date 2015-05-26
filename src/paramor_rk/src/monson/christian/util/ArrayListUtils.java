/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.util.ArrayList;
import java.util.Random;

public class ArrayListUtils {

	// Just a little test method for shuffleInPlace().
	public static void main(String[] args) {
		ArrayList<String> toShuffle = new ArrayList<String>();
		toShuffle.add("a");
		toShuffle.add("b");
		toShuffle.add("c");
		toShuffle.add("d");
		toShuffle.add("e");
		toShuffle.add("f");
		toShuffle.add("g");
		toShuffle.add("h");
		toShuffle.add("i");
		toShuffle.add("j");
		toShuffle.add("k");
		toShuffle.add("l");
		toShuffle.add("m");
		toShuffle.add("n");
		toShuffle.add("o");
		
		System.out.println("Before Shuffling:");
		System.out.println();
		System.out.println(toShuffle.toString());
		System.out.println();

		ArrayListUtils.shuffleInPlace(toShuffle);

		System.out.println("After Shuffling");
		System.out.println();
		System.out.println(toShuffle.toString());
		System.out.println();
		System.out.println();	}
	
	/** 
	 * You can't create an ArrayListUtils
	 */
	private ArrayListUtils() { }
	
	/**
	 * Implements the following algorithm which directly performs random selection without replacement:
	 * 
	 * given an ArrayList:
	 * 
	 * 0 1 2 3 4 5 6
	 * a b c d e f g
	 * 
	 * picks a random number, r, 0 <= r < length of arrayList, let's say 3.
	 * Swaps the item at 3 for the item in the last position of the ArrayList:
	 * 
	 * 0 1 2 3 4 5 6
	 * a b c g e f|d
	 * 
	 * picks another random number, r, such that 0 <= r < one_less_than_the_size_of_arraylist.
	 * Suppose we pick 2.
	 * Swaps the item at 2 for the next to last item:
	 * 
	 * 0 1 2 3 4 5 6
	 * a b f g e|c d
	 * 
	 * picks another random number between 0 and 2 less than the size of the arraylist
	 * Suppose we pick 3 again
	 * And we swap:
	 * 
	 * 0 1 2 3 4 5 6
	 * a b f e|g c d
	 * 
	 * And we continue.
	 * 
	 * Suppose we (again) pick 3 (Which is largest available index we can currently pick)
	 * 
	 * 0 1 2 3 4 5 6 
	 * a b f|e g c d
	 * 
	 * Now we pick 0
	 * 
	 * 0 1 2 3 4 5 6
	 * f b|a e g c d
	 * 
	 * and pick 0 again
	 * 
	 * 0 1 2 3 4 5 6
	 * b|f a e g c d
	 * 
	 * And we are done.
	 * 
	 * We now have an ArrayList that has been randomized--at each step the front portion
	 * of the ArrayList is our urn of balls. We select one ball at random, and place it
	 * in line at the end of the ArrayList. Then draw a new ball from those that remain
	 * in the urn.
	 * 
	 * @param <T>
	 * @param list
	 */
	public static <T> void shuffleInPlace(ArrayList<T> list) {
		
		Random randomNumberGenerator = new Random();
		
		for (int iterationNumber=0; iterationNumber<list.size(); iterationNumber++) {
			int indexOfLastItemNotYetRandomized = list.size() - iterationNumber - 1;
			
			int randomIndex = randomNumberGenerator.nextInt(indexOfLastItemNotYetRandomized + 1);
			
			// swap the item at the randomIndex with the item located at
			// the last index of list that does not yet contain a random 
			// item. (I know that it would be more efficient to just have
			// one temporary variable. But for my sanity, I just made two).
			T randomItem = list.get(randomIndex);
			T lastItemNotYetRandomized = list.get(indexOfLastItemNotYetRandomized);
			list.set(randomIndex, lastItemNotYetRandomized);
			list.set(indexOfLastItemNotYetRandomized, randomItem);
		}
	}
	
}
