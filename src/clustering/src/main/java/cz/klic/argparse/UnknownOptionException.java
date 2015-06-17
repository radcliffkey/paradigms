package cz.klic.argparse;

public class UnknownOptionException extends Exception {

	private static final long serialVersionUID = 390674072119408263L;
	private String optionName;
	private int position;

	public UnknownOptionException(String optionName, int position) {
		this.optionName = optionName;
		this.position = position;
	}

	@Override
	public String getMessage() {
		return String.format("Unknown option '%s' at position %d", this.optionName, this.position + 1);
	}

}
