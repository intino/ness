package io.intino.ness.master.data.validation.validators;

import io.intino.ness.master.data.validation.Issue;
import io.intino.ness.master.data.validation.RecordValidator;
import io.intino.ness.master.data.validation.TripletRecordStore;
import io.intino.ness.master.data.validation.TripletSource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ness.master.data.validation.Issue.Type.DUPLICATED_ATTRIBUTE;
import static java.util.Objects.requireNonNull;

public class DuplicatedTripletRecordValidator implements RecordValidator {

	private final Issue.Level level;

	public DuplicatedTripletRecordValidator() {
		this(Issue.Level.Error);
	}

	public DuplicatedTripletRecordValidator(Issue.Level level) {
		this.level = requireNonNull(level);
	}

	@Override
	public Stream<Issue> validate(TripletRecord record, TripletRecordStore store) {
		return record.attributes().entrySet()
				.stream()
				.filter(e -> e.getValue().size() > 1)
				.map(e -> getIssue(record, e));
	}

	private Issue getIssue(TripletRecord record, Map.Entry<String, List<TripletRecord.Value>> e) {
		TripletSource source0 = e.getValue().get(0).source();

		if (e.getValue().stream().allMatch(v -> Objects.equals(source0, v.source())))
			return Issue.create(level, DUPLICATED_ATTRIBUTE, "Record (" + record.id() + ") defines " + e.getKey() + " " + e.getValue().size() + " times.")
					.source(e.getValue().get(e.getValue().size() - 1).source());

		return Issue.create(level, DUPLICATED_ATTRIBUTE, "Record (" + record.id() + ") defines " + e.getKey() + " " + e.getValue().size() + " times in different files.")
				.source(new CombinedTripletSource(e.getValue().stream().map(TripletRecord.Value::source).map(TripletSource::get).collect(Collectors.toList())));
	}

	public static class CombinedTripletSource implements TripletSource {

		private final List<String> names;

		public CombinedTripletSource(List<String> names) {
			this.names = names;
		}

		public List<String> names() {
			return names;
		}

		@Override
		public String name() {
			return "Duplicated fields across different sources";
		}

		@Override
		public String get() {
			return "Sources:\n\t\t" + String.join("\n\t\t", names);
		}
	}
}
