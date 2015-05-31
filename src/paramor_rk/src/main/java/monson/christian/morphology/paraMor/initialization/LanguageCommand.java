/**
 * 
 */
package monson.christian.morphology.paraMor.initialization;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monson.christian.morphology.paraMor.ParaMor;
import monson.christian.morphology.paraMor.languages.Language;

class LanguageCommand extends AbstractInitFileCommand {

	public final static String COMMAND_STRING = "language";
	
	/** 
	 * @return A String error message specifying the format for a 'language'
	 *         initializations file command 
	 */
	public static String getCommandUsage() {
		String usage = 
			String.format(COMMAND_STRING + " <language-name>%n" +
						  "  Where the valid <language-name>'s are: " +
						  Language.LanguageName.getPrettyStringOfValidLanguages());
		return usage;
	}

	/**
	 * Attempt to parse the line <code>line</code> as a 'language' command.  If the
	 * 'language' command is successfully parsed then morphologyInducer will use an
	 * instance of the associated Language sub-class.
	 * 
	 * @param morphologyInducer If the 'language' command is successfully parsed then 
	 * 							morphologyInducer will use an instance of the 
	 * 							associated Language sub-class.
	 * @param line The line from the initializations file we belive is a 'language' 
	 *               command.
	 * @param pathToInitFile The path to the initializations file where the 'corpus'
	 *                       command occured. 
	 * @param lineNum The line number from the initializations file where the 'language'
	 *                command occured.
	 * @return <code>true&nbsp;</code> if parse succeeds
	 *     <br><code>false</code>      if parse fails
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	@Override
	public boolean parseCommand(ParaMor morphologyInducer,
								String line, 
								String pathToInitFile, 
								int lineNum) {
		Pattern commandPattern =
			Pattern.compile("^\\s*" + COMMAND_STRING + "\\s+(.+)\\s*$",
							Pattern.CASE_INSENSITIVE);
		Matcher commandMatcher = commandPattern.matcher(line);
		boolean isValidCommand = commandMatcher.matches();
		
		if (isValidCommand) {
			morphologyInducer.setCorpusLanguage(commandMatcher.group(1));
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