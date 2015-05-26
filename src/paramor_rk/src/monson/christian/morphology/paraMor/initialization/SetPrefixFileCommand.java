package monson.christian.morphology.paraMor.initialization;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.util.DebugLog;
import monson.christian.morphology.paraMor.ParaMor;

public class SetPrefixFileCommand extends AbstractInitFileCommand {
	
	public final static String COMMAND_STRING = "prefixList";

	@Override
	public boolean parseCommand(ParaMor morphologyInducer, String line,
			String pathToInitFile, int lineNum) {
		String prefixFileName = null;
		try{
			prefixFileName = line.split("\\s+")[1];
			DebugLog.write("setting prefix file to " + prefixFileName);
			ManualData.setPrefixFileName(prefixFileName);
			ManualData.readPrefixes();
		} catch (Exception e) {
			DebugLog.write(e);
			return false;
		}
		
		return true;
	}

}
