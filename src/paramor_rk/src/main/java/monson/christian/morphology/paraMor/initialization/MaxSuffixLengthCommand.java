/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.ParaMor;

class MaxSuffixLengthCommand extends AbstractInitFileCommand {

	public final static String COMMAND_STRING = "maxSuffixLength";
	
	/** 
	 * @return A String error message specifying the format for a 'maxSuffixLength'
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
	 * Attempt to parse the line <code>line</code> as a 'typesToRead' command.  If the
	 * 'typesToRead' command is successfully parsed then morphologyInducer is told to read
	 * the specified number of types.
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
		Pattern commandPattern =
			Pattern.compile("^\\s*" + COMMAND_STRING + "\\s+(\\d+)\\s*$",
							Pattern.CASE_INSENSITIVE);
		Matcher commandMatcher = commandPattern.matcher(line);
		boolean isValidCommand = commandMatcher.matches();
		
		if (isValidCommand) {
			Corpus corpus = morphologyInducer.getCorpus();
			corpus.setMaxSuffixLength(Integer.valueOf(commandMatcher.group(1)));
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