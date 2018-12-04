package net.vjdv.baz.pe;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class SqlPatterns {
	private static final String[] KEYWORDS = new String[] { "SELECT", "INSERT", "UPDATE", "DELETE", "SET", "EXEC",
			"FROM", "WHERE", "ORDER BY", "GROUP BY", "HAVING", "DESC", "ASC", "TOP", "INTO", "VALUES", "WITH(NOLOCK)" };
	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + "|"
			+ String.join("|", KEYWORDS).toLowerCase() + ")\\b";
	private static final String[] JOINER = new String[] { "IN", "AND", "OR" };
	private static final String JOINER_PATTERN = "\\b(" + String.join("|", JOINER) + ")\\b";
	private static final String NUMBER_PATTERN = "[0-9]";
	private static final String STRING_PATTERN = "'([^']|'')*'";

	public static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<JOINER>"
			+ JOINER_PATTERN + ")" + "|(?<NUMBER>" + NUMBER_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")");

	public static StyleSpans<Collection<String>> computeHighlight(String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass = matcher.group("KEYWORD") != null ? "keyword"
					: matcher.group("JOINER") != null ? "joiner"
							: matcher.group("NUMBER") != null ? "number"
									: matcher.group("STRING") != null ? "string" : null; /* never happens */
			assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

}
