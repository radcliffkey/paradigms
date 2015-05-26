/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.ParaMor;

class SetCorpusStartingPositionByTypeCommand extends AbstractInitFileCommand {

	public final static String COMMAND_STRING = "startReadingCorpusAtFirstNewTypeAfterTypeNumber";
	
	/** 
	 * @return A String error message specifying the format for a 
	 *         'startReadingCorpusAtFirstNewTypeAfterTypeNumber' initializations file command 
	 *         
	 */
	public static String getCommandUsage() {
		String usage =
			String.format(COMMAND_STRING + " <integer>%n" +
						  "  Where <integer> is the number of unique types to skip%n" +
						  "  from the corpus before begining to read from the corpus.%n" +
						  "  Reading will commence with the first novel type after" +
						  "  <integer> types have been skipped.  An <integer> must " +
						  "  consist only of digits.");
		return usage;
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'startReadingCorpusAtFirstNewTypeAfterTypeNumber' 
	 * command.  If the 'startReadingCorpusAtFirstNewTypeAfterTypeNumber' command is successfully parsed then 
	 * morphologyInducer is told to start reading tokens with the first novel type encountered after
	 * the specified number of types have been skipped.
	 * 
	 * @param morphologyInducer If the 'startReadingCorpusAtFirstNewTypeAfterTypeNumber' command is 
	 * 						    successfully parsed then morphologyInducer is told to start 
	 * 							reading tokens with the first novel type encountered after
	 * 							the specified number of types have been skipped.
	 * @param line The line from the initializations file we belive is a  
	 *               'startReadingCorpusAtFirstNewTypeAfterTypeNumber' command.
	 * @param pathToInitFile The path to the initializations file where the 
	 *                       'startReadingCorpusAtFirstNewTypeAfterTypeNumber' command occured. 
	 * @param lineNum The line number from the initializations file where the 
	 *                'startReadingCorpusAtFirstNewTypeAfterTypeNumber' command occured.
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
			corpus.setTokensToSkip(Integer.valueOf(commandMatcher.group(1)));
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