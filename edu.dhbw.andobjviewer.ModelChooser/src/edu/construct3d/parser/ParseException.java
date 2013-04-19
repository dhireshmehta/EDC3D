package edu.construct3d.parser;

public class ParseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseException(String file,int lineNumber, String msg) {
		super("Parse error in file "+file+"on line "+lineNumber+":"+msg);
	}
}
