package cz.klic.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fileutil {
	
	public static BufferedReader getBufReader(String fileName, String encoding) throws IOException {
		InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), encoding);
		BufferedReader reader = new BufferedReader(isr);
		return reader;
	}
	
	public static BufferedReader getBufReader(String fileName) throws IOException {
		BufferedReader reader = getBufReader(fileName, "UTF-8");
		return reader;
	}
	
	public static BufferedWriter getBufWriter(String fileName, String encoding) throws IOException {
		OutputStreamWriter isr = new OutputStreamWriter(new FileOutputStream(fileName), encoding);
		BufferedWriter writer = new BufferedWriter(isr);
		return writer;
	}
	
	public static BufferedWriter getBufWriter(String fileName) throws IOException {
		BufferedWriter writer = getBufWriter(fileName, "UTF-8");
		return writer;
	}
	
	public static List<String> readToList(BufferedReader reader) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		String line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		
		reader.close();
		
		return lines;
	}
	
	/**
	 * Reads a text file and returns the list of its lines
	 * @param fileName
	 * @return list of lines in the file
	 * @throws IOException
	 */
	public static List<String> readToList(String fileName) throws IOException {		
		BufferedReader reader = getBufReader(fileName);
		
		return readToList(reader);
	}
	
	/**
	 * Reads a text file and returns the list of its lines
	 * @param fileName
	 * @return list of lines in the file
	 * @throws IOException
	 */
	public static List<String> readToList(String fileName, String encoding) throws IOException {		
		BufferedReader reader = getBufReader(fileName, encoding);
		
		return readToList(reader);
	}
	
	public static Map<String, String> readMap(String fileName) throws IOException {		
		BufferedReader reader = getBufReader(fileName);
		
		return readMap(reader);
	}

	public static Map<String, String> readMap(String fileName, String encoding) throws IOException {		
		BufferedReader reader = getBufReader(fileName, encoding);
		
		return readMap(reader);
	}
	
	public static Map<String, String> readMap(BufferedReader reader) throws IOException {		
		Map<String, String> result = new HashMap<String, String>();
		
		String line ;
		while ((line = reader.readLine()) != null) {
			String [] fields = line.split("\t", 2);
			result.put(fields[0], fields[1]);
		}
		
		reader.close();
		
		return result;
	}
}
