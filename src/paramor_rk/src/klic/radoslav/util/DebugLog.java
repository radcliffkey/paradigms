package klic.radoslav.util;

import java.io.PrintWriter;

import monson.christian.util.FileUtils;

public class DebugLog {

	private static String fileName = "dbg.log";

	private static DebugLog instance;
	
	private PrintWriter writer;
	
	public void writeMsg(String text) {
		writer.println(text);
	}

	public DebugLog(PrintWriter writer) {
		super();
		this.writer = writer;
	}
	
	public static DebugLog get() {
		if (DebugLog.instance == null) {
			PrintWriter writer = FileUtils.openFileForWriting(fileName, FileUtils.Encoding.UTF8); 
			DebugLog.instance = new DebugLog(writer);			
		}
		return DebugLog.instance;
	}
	
	public static void write(String text) {
		DebugLog.get().writeMsg(text);
	}
	
	public static void write(Object obj) {
		DebugLog.get().writeMsg(obj.toString());
	}
}
