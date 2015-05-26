/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.ParaMor;

class CorpusCommand extends AbstractInitFileCommand {

	public static final String COMMAND_STRING = "corpus";

	/** 
	 * @return A String error message specifying the format for a 'corpus'
	 *         initializations file command 
	 *         
	 */
	public static String getCommandUsage() {
		return "corpus <path-to-text-corpus>";
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'corpus' command.  If the
	 * 'corpus' command is successfully parsed then the file that is specified as the
	 * argument of the 'corpus' command in the initializations file is set as the corpus
	 * of morphologyInducer.
	 * 
	 * @param morphologyInducer If we succeed in parsing this 'corpus' command then
	 * 							morphologyInducer will use the file specified in
	 * 							this command as a its text corpus file.
	 * @param line The line from the initializations file we belive is a 'corpus' command.
	 * @param pathToInitFile The path to the initializations file where the 'corpus'
	 *                       command occured. 
	 * @param lineNum The line number from the initializations file where the 'corpus'
	 *                command occured.
	 * @return  <code>true&nbsp;</code> if parse succeeds
	 *     <br><code>false</code>      if parse fails
	 */
	@Override
	public boolean parseCommand(ParaMor morphologyInducer,
								String line, 
								String pathToInitFile, 
								int lineNum) {
		Pattern corpusCommandPattern = Pattern.compile("^\\s*" + COMMAND_STRING + "\\s+(\\S+)\\s*$", 
													   Pattern.CASE_INSENSITIVE);
		Matcher corpusCommandMatcher = corpusCommandPattern.matcher(line);
		boolean isValidCorpusCommand = corpusCommandMatcher.matches();
		if (isValidCorpusCommand) {
			Corpus corpus = new Corpus();
			corpus.setPathToCorpus(corpusCommandMatcher.group(1));
			morphologyInducer.resetCorpus(corpus);
			
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