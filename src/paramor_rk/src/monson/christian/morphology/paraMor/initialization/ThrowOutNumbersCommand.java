/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.ParaMor;

class ThrowOutNumbersCommand extends AbstractInitFileCommand {

	public final static String COMMAND_STRING = "throwOutNumbers";
	
	/** 
	 * @return A String error message specifying the format for a 'throwOutNumbers'
	 *         initializations file command 
	 *         
	 */
	public static String getCommandUsage() {
		return COMMAND_STRING + " on|off (on is the default)";
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'throwOutNumbers' command.  If 
	 * the 'throwOutNumbers' command is successfully parsed, then morphologyInducer
	 * is informed of the choice of whether or not to throw out numbers from the corpus
	 * of thext that will be read. 
	 * 
	 * @param morphologyInducer If we succeed in parsing this 'throwOutNumbers' command 
	 * 							then morphologyInducer will know whether to throw out
	 * 							numbers in the corpus of read text or not.
	 * @param line The line from the initializations file we belive is a 'throwOutNumbers' 
	 * 			   command.
	 * @param pathToInitFile The path to the initializations file where the 
	 *                       'throwOutNumbers' command occured. 
	 * @param lineNum The line number from the initializations file where the
	 *                'throwOutNumbers' command occured.
	 * @return  <code>true&nbsp;</code> if parse succeeds
	 *     <br><code>false</code>      if parse fails
	 */
	@Override
	public boolean parseCommand(ParaMor morphologyInducer,
								String line, 
								String pathToInitFile, 
								int lineNum) {
		Pattern throwOutNumbersCommandPattern 
			= Pattern.compile("^\\s*" + COMMAND_STRING + "\\s+(\\S+)\\s*$", 
							  Pattern.CASE_INSENSITIVE);
		Matcher throwOutNumbersCommandMatcher 
			= throwOutNumbersCommandPattern.matcher(line);
		boolean isValidThrowOutNumbersCommand = throwOutNumbersCommandMatcher.matches();
		if (isValidThrowOutNumbersCommand) {
			String throwOutNumbers = throwOutNumbersCommandMatcher.group(1);
			
			Corpus corpus = morphologyInducer.getCorpus();
			if (throwOutNumbers.matches("(on)|(On)|(ON)")) {
				corpus.setThrowOutNumbers(true);				
			} else {
				corpus.setThrowOutNumbers(false);
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