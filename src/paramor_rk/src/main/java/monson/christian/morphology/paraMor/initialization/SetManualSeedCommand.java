package monson.christian.morphology.paraMor.initialization;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.util.DebugLog;
import monson.christian.morphology.paraMor.ParaMor;

public class SetManualSeedCommand extends AbstractInitFileCommand {
	
	public final static String COMMAND_STRING = "manualSeed";

	@Override
	public boolean parseCommand(ParaMor morphologyInducer, String line,
			String pathToInitFile, int lineNum) {
		String seedFileName = null;
		try{
			seedFileName = line.split("\\s+")[1];
			DebugLog.write("setting man. seed file to " + seedFileName);
			ManualData.setSeedFileName(seedFileName);
			ManualData.getManualAnalyses();
		} catch (Exception e) {
			DebugLog.write(e);
			return false;
		}
		
		return true;
	}

}
