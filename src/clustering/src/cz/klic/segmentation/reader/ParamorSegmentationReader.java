package cz.klic.segmentation.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cz.klic.segmentation.StringMapSegmentation;
import cz.klic.util.Fileutil;

public class ParamorSegmentationReader implements SegmentationReader<String> {

	public static String DEF_ENCODING = "UTF-8";
	private BufferedReader inputStream;
	
	public ParamorSegmentationReader(String fileName) throws IOException {
		this(Fileutil.getBufReader(fileName, DEF_ENCODING));
	}
	
	public ParamorSegmentationReader(String fileName, String encoding) throws IOException {
		this(Fileutil.getBufReader(fileName, encoding));
	}

	public ParamorSegmentationReader(BufferedReader inputStream) {
		super();
		this.inputStream = inputStream;
	}
	
	@Override
	public StringMapSegmentation readSegmentation() throws Exception {
		Map<String, String []> segMap = new HashMap<String, String []>();
		String line;
		while ((line = this.inputStream.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			String [] splitLine = line.split("\t");
			String form = splitLine[0];
			String segmentsStr = splitLine[1];
			String [] segments = segmentsStr.split(" ");
			for (int i = 1; i < segments.length; ++i) {
				//remove the leading "+"
				segments[i] = segments[i].substring(1);
			}
			
			segMap.put(form, segments);
		}
		
		StringMapSegmentation seg = new StringMapSegmentation(segMap);
		return seg;
	}

}
