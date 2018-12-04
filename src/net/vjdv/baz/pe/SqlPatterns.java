package net.vjdv.baz.pe;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class SqlPatterns {
	private static final String[] KEYWORDS = new String[] { "SELECT", "INSERT", "UPDATE", "DELETE", "SET", "EXEC",
			"FROM", "WHERE", "ORDER BY", "GROUP BY", "HAVING", "DESC", "ASC", "TOP", "INTO", "VALUES", "WITH", "NOLOCK",
			"CREATE", "ALTER", "PROCEDURE", "FUNCTION", "PIVOT", "ON", "FOR", "XML", "PATH", "ROOT", "AS", "BEGIN",
			"END", "TRY", "CATCH", "GO", "DECLARE", "INT", "CHAR", "VARCHAR", "DECIMAL", "DATE", "DATETIME", "BIT",
			"OUTPUT", "ADD", "NOT", "NULL", "DEFAULT", "IF", "ELSE", "WHILE" };
	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + "|"
			+ String.join("|", KEYWORDS).toLowerCase() + ")\\b";
	private static final String[] JOINERS = new String[] { "IN", "AND", "OR", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN",
			"BETWEEN" };
	private static final String JOINER_PATTERN = "\\b(" + String.join("|", JOINERS) + ")\\b";
	private static final String[] FUNCS = new String[] { "GETDATE", "ISNULL", "RTRIM", "LTRIM",
			"SP_XML_PREPAREDOCUMENT", "SP_XML_REMOVEDOCUMENT", "OPENXML", "JSON_VALUE", "ISJSON", "JSON_QUERY",
			"JSON_MODIFY", "CONVERT", "CAST" };
	private static final String FUNC_PATTERN = "\\b(" + String.join("|", FUNCS) + "|"
			+ String.join("|", FUNCS).toLowerCase() + ")\\b";
	private static final String STRING_PATTERN = "'([^']|'')*('|\\z)";
	private static final String COMMENT_PATTERN = "(--.*?$)|(/\\*.*?\\*/)";

	private static final Pattern PATTERN = Pattern.compile(
			"(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<JOINER>" + JOINER_PATTERN + ")" + "|(?<FUNC>" + FUNC_PATTERN
					+ ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")",
			Pattern.MULTILINE | Pattern.DOTALL);

	public static StyleSpans<Collection<String>> computeHighlight(String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass = matcher.group("KEYWORD") != null ? "keyword"
					: matcher.group("JOINER") != null ? "joiner"
							: matcher.group("FUNC") != null ? "func"
									: matcher.group("STRING") != null ? "string"
											: matcher.group("COMMENT") != null ? "comment" : null;
			assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

}
