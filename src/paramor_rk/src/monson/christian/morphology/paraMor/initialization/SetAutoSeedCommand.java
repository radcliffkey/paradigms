package monson.christian.morphology.paraMor.initialization;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.util.DebugLog;
import monson.christian.morphology.paraMor.ParaMor;

public class SetAutoSeedCommand extends AbstractInitFileCommand {
	
	public final static String COMMAND_STRING = "autoSeed";

	@Override
	public boolean parseCommand(ParaMor morphologyInducer, String line,
			String pathToInitFile, int lineNum) {
		String seedFileName = null;
		try{
			seedFileName = line.split("\\s+")[1];
			DebugLog.write("setting auto seed file to " + seedFileName);
			ManualData.setAutoSeedFileName(seedFileName);
			ManualData.getAutoAnalyses();
		} catch (Exception e) {
			DebugLog.write(e);
			return false;
		}
		
		return true;
	}

}
