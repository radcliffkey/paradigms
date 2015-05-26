package cz.klic.test;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestScanner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String line = "<csts lang=cs>";
		Scanner sc = new Scanner(line);
		sc.useDelimiter("");
		
		System.out.println(sc.next(".*"));
		
		Pattern p = Pattern.compile("<[^<>]+>");
		Matcher m = p.matcher(line);
		m.find();		
		
		System.out.println(m.group());
		
		String word = "ohrožen";
		p = Pattern.compile("\\p{L}+");
		m = p.matcher(word);
		m.find();		
		
		System.out.println(m.group());
		
		for (int i = 0; i < word.length(); ++i) {
			char c = word.charAt(i);
			System.out.printf("%d: %b\n", i, Character.isLetterOrDigit(c));
		}
		
		p = Pattern.compile("[\\p{L}\\d']+(-\\d+)?");
		m = p.matcher("čech'-1_^(*1ý)");
		m.find();
		System.out.println(m.group());
		
	}

}
