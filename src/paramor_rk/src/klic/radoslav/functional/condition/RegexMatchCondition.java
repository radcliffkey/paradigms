package klic.radoslav.functional.condition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexMatchCondition implements Condition<String> {

	private Pattern pattern;
	
	public RegexMatchCondition(Pattern pattern) {
		super();
		this.pattern = pattern;
	}
	
	public RegexMatchCondition(String pattern) {
		this(Pattern.compile(pattern));
	}

	@Override
	public boolean isTrue(String o1) {
		Matcher matcher = this.pattern.matcher(o1);
		return matcher.matches();
	}

}
