/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

/**
 * Provides static classes to handle simple
 * 
 * @author Christian Monson
 *
 */
public class FileUtils {
	
	/** 
	 * You can't create a FileUtils
	 */
	private FileUtils() { }
	
	/**
	 * The {@link InputStreamReader} class defines a constructor for which you can define the character
	 * encoding, but it takes a String argument that is easy to confuse, and hard to remember--so this
	 * little enum, saves these magic encoding strings.
	 * 
	 * @author Christian Monson
	 *
	 */
	public enum Encoding {
		UTF8("UTF-8"),
		EIGHT_BIT("ISO-8859-1");
		
		private Encoding(String encodingString) {
			this.encodingString = encodingString;
		}
		
		private String encodingString;
		
		@Override
		public String toString() {
			return encodingString;
		}
	}

	public static BufferedReader openFileForReading(File fileToOpen, Encoding encoding) { 
		
		BufferedReader bufferedReaderToReturn = null;
		
		try {
			if (fileToOpen.getName().matches("^.*\\.gz$")) {
	
				// Reading gzipped files
				bufferedReaderToReturn = 
					new BufferedReader(
							new InputStreamReader(
									new GZIPInputStream (
											new FileInputStream(fileToOpen)),
									encoding.toString()));
			} else {
				// Reading un-gzipped files
				bufferedReaderToReturn = 
					new BufferedReader(
							new InputStreamReader(
									new FileInputStream(fileToOpen),
								    encoding.toString()));			
			}
		}
		catch(FileNotFoundException e) {	
			System.err.println();
			System.err.println("  Sorry.  The file: " + fileToOpen.getAbsolutePath());
			System.err.println("    could not be read.  Here is the full Java error:");
			System.err.println();
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println();
			System.exit(0);
		}
		catch(Exception e) {
			System.err.println();
			System.err.println("  Sorry.  While opening the file: " + fileToOpen.getAbsolutePath());
			System.err.println("    an error was encountered.  Here is the full Java error:");
			System.err.println();
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println();
			System.exit(0);
		}
		
		return bufferedReaderToReturn;
	}
	
	public static BufferedReader openFileForReading(String filenameToOpen, Encoding encoding) {
		return openFileForReading(new File(filenameToOpen), encoding);
	}

	public static PrintWriter openFileForWriting(File fileToOpen, Encoding encoding) {
	
		PrintWriter printWriterToReturn = null;
		
		try {
			printWriterToReturn = 
				new PrintWriter(
						new BufferedWriter(
								new OutputStreamWriter(
										new FileOutputStream(fileToOpen),
										encoding.toString())),
						true); // true to autoflush
			
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + fileToOpen.getAbsolutePath());
			System.err.println();
			System.exit(0);
	
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.exit(0);
		}
		
		return printWriterToReturn;
	}
	
	public static PrintWriter openFileForWriting(String filenameToOpen, Encoding encoding) {
		return openFileForWriting(new File(filenameToOpen), encoding);
	}

}
