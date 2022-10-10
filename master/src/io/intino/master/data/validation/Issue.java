package io.intino.master.data.validation;

import static io.intino.master.data.validation.TripleSource.FileTripleSource;
import static io.intino.master.data.validation.TripleSource.PublisherTripleSource;
import static java.util.Objects.requireNonNull;

public class Issue implements Comparable<Issue> {

	public static Issue error(String type, String message) {
		return new Issue(type, Level.Error, message);
	}

	public static Issue warning(String type, String message) {
		return new Issue(type, Level.Warning, message);
	}

	public static Issue create(Level level, String type, String message) {
		return new Issue(type, level, message);
	}

	private final String type;
	private final Level level;
	private final String message;
	private ValidationLayers.Scope scope;
	private TripleSource source;

	public Issue(String type, Level level, String message) {
		this.type = requireNonNull(type);
		this.level = level;
		this.message = message;
	}

	public String type() {
		return type;
	}

	public Level level() {
		return level;
	}

	public String message() {
		return message;
	}

	public Issue scope(ValidationLayers.Scope scope) {
		this.scope = scope;
		return this;
	}

	public TripleSource source() {
		return source;
	}

	public Issue source(TripleSource source) {
		this.source = source;
		return this;
	}

	public String levelMsg() {
		return "[" + level.name().toUpperCase() + "] [" + type + "] " + message;
	}

	@Override
	public String toString() {
		return levelMsg() + (source == null ? "" : "\n\t" + source.get());
	}

	@Override
	public int compareTo(Issue o) {
		if(o == null) return -1;
		return level == o.level ? compareSources(o.source) : level.compareTo(o.level);
	}

	private int compareSources(TripleSource otherSource) {
		if(source == null) return 1;
		if(otherSource == null) return -1;
		if(source instanceof PublisherTripleSource) return 1;
		if(otherSource instanceof PublisherTripleSource) return 1;
		if(source instanceof FileTripleSource && otherSource instanceof FileTripleSource)
			return Integer.compare(((FileTripleSource) source).line(), ((FileTripleSource) otherSource).line());
		return 0;
	}

	public enum Level {
		Error, Warning
	}

	public static class Type {
		public static final String SYNTAX_ERROR = "Syntax error";
		public static final String SUBJECT_WITHOUT_TYPE = "Subject without type";
		public static final String MISSING_ATTRIBUTE = "Missing attribute";
		public static final String DUPLICATED_ATTRIBUTE = "Duplicated attribute";
		public static final String INVALID_VALUE = "Invalid value";
		public static final String INVALID_REFERENCE = "Invalid reference";
	}
}
