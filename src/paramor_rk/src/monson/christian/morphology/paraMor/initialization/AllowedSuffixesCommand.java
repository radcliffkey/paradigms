/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import klic.radoslav.settings.Settings;
import monson.christian.morphology.paraMor.ParaMor;

class AllowedSuffixesCommand extends AbstractInitFileCommand {

	public final static String COMMAND_STRING = "allowedSuffixes";
	
	/** 
	 * @return A String error message specifying the format for a 'allowedSuffixes'
	 *         initializations file command 
	 *         
	 */
	public static String getCommandUsage() {
		String usage = 
			String.format(COMMAND_STRING + " <integer>%n" +
						  "  Where <integer> is the maximal allowed length of a suffix");
		return usage;
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'allowedSuffixes' command.
	 * 
	 * @param morphologyInducer If the 'maxSuffixLength' command is successfully parsed then 
	 * 							morphologyInducer limit the length of suffixes.
	 * @param line The line from the initializations file we belive is a 'maxSuffixLength' 
	 *               command.
	 * @param pathToInitFile The path to the initializations file where the 'maxSuffixLength'
	 *                       command occured. 
	 * @param lineNum The line number from the initializations file where the 'maxSuffixLength'
	 *                command occured.
	 * @return <code>true&nbsp;</code> if parse succeeds
	 *     <br><code>false</code>      if parse fails
	 */
	@Override
	public boolean parseCommand(ParaMor morphologyInducer,
								String line, 
								String pathToInitFile, 
								int lineNum) {
		
		String affixListStr = line.split("\\s+", 2)[1];
		String[] affixList = affixListStr.split(",");
		
		boolean isValidCommand = affixList.length > 0;
		
		if (isValidCommand) {
			List<Pattern> allowedAffixes = new ArrayList<Pattern>();
			for (String affix : affixList) {
				allowedAffixes.add(Pattern.compile(affix.trim()));
			}
			Settings.setOption(COMMAND_STRING, allowedAffixes);
		} else {
			String errorMsg =
				String.format("Badly formatted '" + COMMAND_STRING + "' command%n%n" +
							  getCommandUsage() + "%n");
			handleParseError(errorMsg, pathToInitFile, lineNum);
			return false;
		}
		
		return true;
	}
}