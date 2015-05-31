package monson.christian.morphology.paraMor.initialization;

import java.util.HashSet;
import java.util.List;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.morphology.phonChange.RuleFileParser;
import klic.radoslav.morphology.phonChange.StemChangeRule;
import klic.radoslav.util.DebugLog;
import monson.christian.morphology.paraMor.ParaMor;

public class SetManualRuleFileCommand extends AbstractInitFileCommand {
	
	public final static String COMMAND_STRING = "manualRuleFile";

	@Override
	public boolean parseCommand(ParaMor morphologyInducer, String line,
			String pathToInitFile, int lineNum) {
		String ruleFileName = null;
		try{
			ruleFileName = line.split("\\s+")[1];
			DebugLog.write("reading manual rules from " + ruleFileName);
			RuleFileParser parser = new RuleFileParser();
			List<StemChangeRule> rules = parser.parseFile(ruleFileName);
			ManualData.setManualRules(new HashSet<StemChangeRule>(rules));
		} catch (Exception e) {
			DebugLog.write(e);
			return false;
		}
		
		return true;
	}

}
