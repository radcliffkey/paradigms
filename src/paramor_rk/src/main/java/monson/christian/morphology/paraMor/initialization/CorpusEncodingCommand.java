/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.ParaMor;

class CorpusEncodingCommand extends AbstractInitFileCommand {

	public final static String COMMAND_STRING = "corpusEncoding";
	
	/** 
	 * @return A String error message specifying the format for a 'corpusEncoding'
	 *         initializations file command 
	 */
	public static String getCommandUsage() {
		return COMMAND_STRING + " <encoding-name> (UTF-8 is the default)";
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'corpusEncoding' command.  If 
	 * the 'corpusEncoding' command is successfully parsed, then morphologyInducer
	 * is informed of the choice of encoding for the corpus text. 
	 * 
	 * @param morphologyInducer If we succeed in parsing this 'corpusEncoding' command 
	 * 							then morphologyInducer will know the character encoding
	 * 							for this corpus of text.
	 * @param line The line from the initializations file we belive is a 'corpusEncoding' 
	 * 			   command.
	 * @param pathToInitFile The path to the initializations file where the 
	 *                       'corpusEncoding' command occured. 
	 * @param lineNum The line number from the initializations file where the
	 *                'corpusEncoding' command occured.
	 * @return  <code>true&nbsp;</code> if parse succeeds
	 *     <br><code>false</code>      if parse fails
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
			String encoding = commandMatcher.group(1);
			
			Corpus corpus = morphologyInducer.getCorpus();
			corpus.setEncoding(encoding);

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