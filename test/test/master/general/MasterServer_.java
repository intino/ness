package master.general;

import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.BuildInfo;
import com.hazelcast.instance.BuildInfoProvider;
import com.hazelcast.security.SecurityContext;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.data.FileTripletLoader;
import io.intino.ness.master.data.validation.*;
import io.intino.ness.master.data.validation.validators.DuplicatedTripletRecordValidator;
import io.intino.ness.master.data.validation.validators.SyntaxTripletValidator;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.serialization.MasterSerializers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.ness.master.data.validation.Issue.Type.INVALID_VALUE;
import static io.intino.ness.master.data.validation.Issue.Type.MISSING_ATTRIBUTE;

public class MasterServer_ {


	public static void main(String[] args) {
//		initMaster();
		initHazelcastRaw();
	}

	private static void initMaster() {
		Master.Config config = new Master.Config();
		config.datalakeRootPath(new File("temp/cinepolis-data/datasets"));
		config.instanceName("the server");
		config.serializer(MasterSerializers.getDefault());
		config.tripletsLoader(new FileTripletLoader(new File("temp/cinepolis-data/datasets")));
		config.port(62555);
		config.putProperty("hazelcast.logging.type", "none");

		Master master = new Master(config);
		master.start();

		Runtime.getRuntime().addShutdownHook(new Thread(master::stop));
	}

	private static void initHazelcastRaw() {

		BuildInfo buildInfo = BuildInfoProvider.getBuildInfo();

//		try {
//			Field modifiersField = Field.class.getDeclaredField("modifiers");
//			modifiersField.setAccessible(true);
//
//			Field f = buildInfo.getClass().getDeclaredField("enterprise");
//			f.setAccessible(true);
//			modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
//
//			f.set(buildInfo, true);
//
//			System.out.println(buildInfo.isEnterprise());
//
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//		SecurityConfig sc = new SecurityConfig();
//		sc.setClientRealmConfig("realm", new RealmConfig().setUsernamePasswordIdentityConfig("client1", "client1_password"));
//		sc.setClientRealm("realm");
//		sc.setMemberRealm("realm");
//		sc.setEnabled(true);
//
//		SocketInterceptorConfig socketInterceptorConfig = new SocketInterceptorConfig()
//				.setEnabled(true)
//				.setClassName(MySocketInterceptor.class.getName());

		Config config = new Config();
		config.setClusterName("cluster");
		config.setInstanceName("cluster");
		config.setNetworkConfig(new NetworkConfig().setPort(62555));
//		config.setSecurityConfig(sc);

		HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

		Runtime.getRuntime().addShutdownHook(new Thread(hz::shutdown));
	}

	public static void main1(String[] args) {
		Master.Config config = new Master.Config();
		config.datalakeRootPath(new File("temp/cinepolis-data/datasets"));
		config.instanceName("Master");
		config.serializer(MasterSerializers.getDefault());
		config.port(62555);

		ValidationLayers validationLayers = new ValidationLayers();
		validationLayers.tripleValidationLayer().addValidator(new SyntaxTripletValidator());

		validationLayers.recordValidationLayer().setValidator("theater", (record, store) -> {
			if(record.get("ipSegment").isEmpty()) return Stream.of(Issue.warning(MISSING_ATTRIBUTE, "Theater does not have ipSegment").source(record.source()));
			RecordValidator.TripletRecord.Value value = record.get("ipSegment").get(0);
			if(!value.get().endsWith(".")) return Stream.of(Issue.error(INVALID_VALUE, "ipSegment must end with .").source(value.source()));
			return Stream.empty();
		});

//		config.validationLayers(validationLayers);

		Master master = new Master(config);
		master.start();

//		master.add("test", "Hola\tQu e\tTal");
//		master.add("test", new Triple("1234567:theater", "ipSegment", "123"));
	}

	private static Stream<Issue> validateTheaterIpSegment(Triplet triplet, TripletSource source) {
		if(!triplet.type().equals("theater")) return null;
		if(!triplet.predicate().equals("ipSegment")) return null;

		return triplet.value().endsWith(".") ? null : Stream.of(Issue.error(INVALID_VALUE, "IpSegment must end with ."));
	}

	private static Stream<Issue> validateTheaterId(Triplet triplet, TripletSource source) {
		if(!triplet.type().equals("theater")) return null;
		if(!isInt(triplet.subject()) || triplet.subject().length() != 7) return Stream.of(Issue.error(INVALID_VALUE, "Theater id must be an integer of 7 digits").source(source));
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

		private final DuplicatedTripletRecordValidator duplicatedTripletRecordValidator = new DuplicatedTripletRecordValidator();

		@Override
		public Stream<Issue> validate(TripletRecord record, TripletRecordStore store) {
			return Stream.concat(
					generalValidation(record, store),
					validationPerField(record, store)
			);
		}

		private Stream<Issue> generalValidation(TripletRecord record, TripletRecordStore store) {
			return duplicatedTripletRecordValidator.validate(record, store);
		}

		private Stream<Issue> validationPerField(TripletRecord record, TripletRecordStore store) {
			return record.attributes().entrySet().stream().map(e -> validate(e, record, store)).reduce(Stream::concat).orElse(Stream.empty());
		}

		private Stream<Issue> validate(Map.Entry<String, List<TripletRecord.Value>> attrib, TripletRecord record, TripletRecordStore store) {
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

		private Stream<Issue> validateCoordinates(List<TripletRecord.Value> value, TripletRecord record, TripletRecordStore store) {
			return Stream.empty();
		}

		private Stream<Issue> validateArea(List<TripletRecord.Value> value, TripletRecord record, TripletRecordStore store) {
			if(value.isEmpty()) throw new RuntimeException();
			TripletRecord.Value v = value.get(0);
			if(v.isEmpty()) throw new RuntimeException();
			TripletRecord area = store.get(v.get());
			if(area == null) throw new RuntimeException();
			return Stream.empty();
		}

		private Stream<Issue> validateEnabled(List<TripletRecord.Value> value, TripletRecord record, TripletRecordStore store) {
			if(value.isEmpty()) return Stream.empty();
			TripletRecord.Value v = value.get(0);
			if(v.isEmpty()) return Stream.empty();
			if(!isBoolean(v.get())) return Stream.of(Issue.error(INVALID_VALUE, "Theater.enabled must be a boolean [false, true], but was " + v.get()).source(v.source()));
			return Stream.empty();
		}

		private boolean isBoolean(String s) {
			return false;
		}
	}

}
