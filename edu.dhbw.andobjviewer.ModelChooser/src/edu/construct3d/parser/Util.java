package edu.construct3d.parser;

import java.util.regex.Pattern;

public class Util {
	
	private static final Pattern trimWhiteSpaces = Pattern.compile("[\\s]+");
	private static final Pattern removeInlineComments = Pattern.compile("#");
	private static final Pattern splitBySpace = Pattern.compile(" ");

	
	/**
	 * returns a canonical line of a obj or mtl file.
	 * e.g. it removes multiple whitespaces or comments from the given string.
	 * @param line
	 * @return
	 */
	public static final String getCanonicalLine(String line) {
		line = trimWhiteSpaces.matcher(line).replaceAll(" ");
		if(line.contains("#")) {
			String[] parts = removeInlineComments.split(line);
			if(parts.length > 0)
				line = parts[0];//remove inline comments
		}
		return line;
	}
	public static String[] splitBySpace(String str) {
		return splitBySpace.split(str);
	}
	
	/**
	 * Trims down obj files, so that they may be parsed faster later on.
	 * Remove uneccessary whitespaces, comments etc.
	 * @param in stream to be trimmed
	 * @param out the resulting trimmed stream
	 */
	
	
}
