package io.intino.master;

import io.intino.master.core.Master;
import io.intino.master.core.MasterConfig;
import io.intino.master.data.validation.*;
import io.intino.master.data.validation.validators.DuplicatedTripleRecordValidator;
import io.intino.master.data.validation.validators.SyntaxTripleValidator;
import io.intino.master.model.Triple;
import io.intino.master.serialization.MasterSerializers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.master.data.validation.Issue.Type.INVALID_VALUE;
import static io.intino.master.data.validation.Issue.Type.MISSING_ATTRIBUTE;

public class TestMasterMain {

	public static void main(String[] args) {
		MasterConfig config = new MasterConfig();
		config.dataDirectory(new File("temp/cinepolis-data/datasets"));
		config.logDirectory(new File("temp/logs/master"));
		config.instanceName("Master");
		config.serializer(MasterSerializers.getDefault());
		config.port(62555);

		Master master = new Master(config);
		master.start();
	}

	public static void main1(String[] args) {
		MasterConfig config = new MasterConfig();
		config.dataDirectory(new File("temp/cinepolis-data/datasets"));
		config.logDirectory(new File("temp/logs/master"));
		config.instanceName("Master");
		config.serializer(MasterSerializers.getDefault());
		config.port(62555);

		ValidationLayers validationLayers = new ValidationLayers();
		validationLayers.tripleValidationLayer().addValidator(new SyntaxTripleValidator());

		validationLayers.recordValidationLayer().setValidator("theater", (record, store) -> {
			if(record.get("ipSegment").isEmpty()) return Stream.of(Issue.warning(MISSING_ATTRIBUTE, "Theater does not have ipSegment").source(record.source()));
			RecordValidator.TripleRecord.Value value = record.get("ipSegment").get(0);
			if(!value.get().endsWith(".")) return Stream.of(Issue.error(INVALID_VALUE, "ipSegment must end with .").source(value.source()));
			return Stream.empty();
		});

//		config.validationLayers(validationLayers);

		Master master = new Master(config);
		master.start();

		master.add("test", "Hola\tQu e\tTal");
//		master.add("test", new Triple("1234567:theater", "ipSegment", "123"));
	}

	private static Stream<Issue> validateTheaterIpSegment(Triple triple, TripleSource source) {
		if(!triple.type().equals("theater")) return null;
		if(!triple.predicate().equals("ipSegment")) return null;

		return triple.value().endsWith(".") ? null : Stream.of(Issue.error(INVALID_VALUE, "IpSegment must end with ."));
	}

	private static Stream<Issue> validateTheaterId(Triple triple, TripleSource source) {
		if(!triple.type().equals("theater")) return null;
		if(!isInt(triple.subject()) || triple.subject().length() != 7) return Stream.of(Issue.error(INVALID_VALUE, "Theater id must be an integer of 7 digits").source(source));
		return null;
	}

	private static boolean isInt(String subject) {
		try {
			Integer.parseInt(subject.replace(":theater", ""));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static class TheaterValidator implements RecordValidator {

		private final DuplicatedTripleRecordValidator duplicatedTripleRecordValidator = new DuplicatedTripleRecordValidator();

		@Override
		public Stream<Issue> validate(TripleRecord record, TripleRecordStore store) {
			return Stream.concat(
					generalValidation(record, store),
					validationPerField(record, store)
			);
		}

		private Stream<Issue> generalValidation(TripleRecord record, TripleRecordStore store) {
			return duplicatedTripleRecordValidator.validate(record, store);
		}

		private Stream<Issue> validationPerField(TripleRecord record, TripleRecordStore store) {
			return record.attributes().entrySet().stream().map(e -> validate(e, record, store)).reduce(Stream::concat).orElse(Stream.empty());
		}

		private Stream<Issue> validate(Map.Entry<String, List<TripleRecord.Value>> attrib, TripleRecord record, TripleRecordStore store) {
			switch(attrib.getKey()) {
//				case "address": return validateAddress(attrib.getValue(), record, store);
//				case "city": return validateCity(attrib.getValue(), record, store);
				case "coordinates": return validateCoordinates(attrib.getValue(), record, store);
//				case "email": return validateEmail(attrib.getValue(), record, store);
//				case "exhibitor": return validateExhibitor(attrib.getValue(), record, store);
//				case "idVista": return validateIdVista(attrib.getValue(), record, store);
//				case "manager": return validateManager(attrib.getValue(), record, store);
//				case "name": return validateName(attrib.getValue(), record, store);
//				case "postalCode": return validatePostalCode(attrib.getValue(), record, store);
//				case "region": return validateRegion(attrib.getValue(), record, store);
//				case "screens": return validateScreens(attrib.getValue(), record, store);
//				case "ipOrder": return validateIpOrder(attrib.getValue(), record, store);
//				case "ipSegment": return validateIpSegment(attrib.getValue(), record, store);
//				case "ipTms": return validateIpTms(attrib.getValue(), record, store);
//				case "shared": return validateShared(attrib.getValue(), record, store);
//				case "telephone": return validateTelephone(attrib.getValue(), record, store);
//				case "territory": return validateTerritory(attrib.getValue(), record, store);
//				case "type": return validateType(attrib.getValue(), record, store);
				case "area": return validateArea(attrib.getValue(), record, store);
				case "enabled": return validateEnabled(attrib.getValue(), record, store);
				default: return Stream.empty();
			}
		}

		private Stream<Issue> validateCoordinates(List<TripleRecord.Value> value, TripleRecord record, TripleRecordStore store) {
			return Stream.empty();
		}

		private Stream<Issue> validateArea(List<TripleRecord.Value> value, TripleRecord record, TripleRecordStore store) {
			if(value.isEmpty()) throw new RuntimeException();
			TripleRecord.Value v = value.get(0);
			if(v.isEmpty()) throw new RuntimeException();
			TripleRecord area = store.get(v.get());
			if(area == null) throw new RuntimeException();
			return Stream.empty();
		}

		private Stream<Issue> validateEnabled(List<TripleRecord.Value> value, TripleRecord record, TripleRecordStore store) {
			if(value.isEmpty()) return Stream.empty();
			TripleRecord.Value v = value.get(0);
			if(v.isEmpty()) return Stream.empty();
			if(!isBoolean(v.get())) return Stream.of(Issue.error(INVALID_VALUE, "Theater.enabled must be a boolean [false, true], but was " + v.get()).source(v.source()));
			return Stream.empty();
		}

		private boolean isBoolean(String s) {
			return false;
		}
	}
}
