/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.initialization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommandFactory {
	static AbstractInitFileCommand getCommand(String line,
							  String pathToInitFile,
							  int lineNum) {
			
		if (lineMatches(line, CorpusCommand.COMMAND_STRING)) {
			return new CorpusCommand();
			
		} else if (lineMatches(line, ThrowOutNumbersCommand.COMMAND_STRING)) {
			return new ThrowOutNumbersCommand();
			
		} else if (lineMatches(line, CorpusEncodingCommand.COMMAND_STRING)) {
			return new CorpusEncodingCommand();
			
		} else if (lineMatches(line, CaseSensitiveCommand.COMMAND_STRING)) {
			return new CaseSensitiveCommand();
			
		} else if (lineMatches(line, LanguageCommand.COMMAND_STRING)) {
			return new LanguageCommand();
			
		} else if (lineMatches(line, TokensToReadCommand.COMMAND_STRING)) {
			return new TokensToReadCommand();
			
		} else if (lineMatches(line, TypesToReadCommand.COMMAND_STRING)) {
			return new TypesToReadCommand();
			
		} else if (lineMatches(line, SetCorpusStartingPositionByTokenCommand.COMMAND_STRING)) {
			return new SetCorpusStartingPositionByTokenCommand();
			
		} else if (lineMatches(line, SetCorpusStartingPositionByTypeCommand.COMMAND_STRING)) {
			return new SetCorpusStartingPositionByTypeCommand();
		
		} else if (lineMatches(line, SetManualSeedCommand.COMMAND_STRING)) {
			return new SetManualSeedCommand();
		
		} else if (lineMatches(line, SetAutoSeedCommand.COMMAND_STRING)) {
			return new SetAutoSeedCommand();
		
		} else if (lineMatches(line, SetPrefixFileCommand.COMMAND_STRING)) {
			return new SetPrefixFileCommand();
		
		} else if (lineMatches(line, MaxSuffixLengthCommand.COMMAND_STRING)) {
			return new MaxSuffixLengthCommand();
		
		} else if (lineMatches(line, AllowedSuffixesCommand.COMMAND_STRING)) {
			return new AllowedSuffixesCommand();
		
		} else if (lineMatches(line, SetManualRuleFileCommand.COMMAND_STRING)) {
			return new SetManualRuleFileCommand();
		
		} else {
			String errorMsg;
			errorMsg = String.format("Unrecognized command in initFile: \""
									 + line + "\"");
			AbstractInitFileCommand.handleParseError(errorMsg, pathToInitFile, lineNum);
			System.exit(0);
		}

		// can never actually reach this line.  But to appease the compiler...
		return null;
	}
	
	private static boolean lineMatches(String line, String commandString) {
		Pattern commandPattern = Pattern.compile("\\s*" + commandString + "\\s.*", 
						  						 Pattern.CASE_INSENSITIVE);
		Matcher commandMatcher = commandPattern.matcher(line);
		boolean isValidCommand = commandMatcher.matches();
		
		return isValidCommand;
	}
}
