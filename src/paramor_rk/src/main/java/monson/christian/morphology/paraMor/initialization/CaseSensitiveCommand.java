/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.ParaMor;

class CaseSensitiveCommand extends AbstractInitFileCommand {

	public static final String COMMAND_STRING = "caseSensitive";
	
	/** 
	 * @return A String error message specifying the format for a 'caseSensitive'
	 *         initializations file command 
	 */
	public static String getCommandUsage() {
		return COMMAND_STRING + " on|off (off, (case insensitive) is the default)";
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'caseSensitive' command.  If 
	 * the 'caseSensitive' command is successfully parsed, then morphologyInducer
	 * is informed of the choice for case sensitivity.   
	 * 
	 * @param morphologyInducer If we succeed in parsing this 'corpusEncoding' command 
	 * 							then morphologyInducer is informed of the choice of case
	 * 							sensitivity.
	 * @param line The line from the initializations file we belive is a 'caseSensitive' 
	 * 			   command.
	 * @param pathToInitFile The path to the initializations file where the 
	 *                       'caseSensitive' command occured. 
	 * @param lineNum The line number from the initializations file where the
	 *                'caseSensitive' command occured.
	 * @return  <code>true&nbsp;</code> if parse succeeds
	 *      <br><code>false</code>      if parse fails
	 */
	@Override
	public boolean parseCommand(ParaMor morphologyInducer,
								String line, 
								String pathToInitFile, 
								int lineNum) {
		Pattern commandPattern 
			= Pattern.compile("^\\s*" + COMMAND_STRING + "\\s+(\\S+)\\s*$", 
							  Pattern.CASE_INSENSITIVE);
		Matcher commandMatcher = commandPattern.matcher(line);
		boolean isValidCommand = commandMatcher.matches();
		if (isValidCommand) {
			String caseSensitivity = commandMatcher.group(1);
			
			Corpus corpus = morphologyInducer.getCorpus();
			if (caseSensitivity.matches("(on)|(On)|(ON)")) {
				corpus.setCaseSensitivity(true);				
			} else {
				corpus.setCaseSensitivity(false);
			}

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