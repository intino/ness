package io.intino.datahub.box;

import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;
import io.intino.alexandria.ui.services.AuthService;
import io.intino.datahub.box.service.jms.NessService;
import io.intino.datahub.box.service.scheduling.Sentinels;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.broker.jms.JmsBrokerService;
import io.intino.datahub.datalake.BrokerSessions;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.EntityData;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.data.ComponentAttributeDefinition;
import io.intino.ness.master.data.ComponentsTripletsDigester;
import io.intino.ness.master.data.EntityLoader;
import io.intino.ness.master.data.MasterTripletsDigester;
import io.intino.ness.master.data.MasterTripletsDigester.Result.Stats;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.serialization.MasterSerializers;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataHubBox extends AbstractBox {

	private FileDatalake datalake;
	private BrokerService brokerService;
	private BrokerSessions brokerSessions;
	private NessService nessService;
	private Sentinels sentinels;
	private NessGraph graph;
	private Instant lastSeal;
	private Master master;

	public DataHubBox(String[] args) {
		super(args);
	}

	public DataHubBox(DataHubConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		if (o instanceof Graph) {
			graph = ((Graph) o).as(NessGraph.class);
			injectJmsConfiguration();
		}
		if (o instanceof NessGraph) {
			graph = (NessGraph) o;
			injectJmsConfiguration();
		}
		return this;
	}

	public BrokerService brokerService() {
		return brokerService;
	}

	public NessGraph graph() {
		return graph;
	}

	public BrokerSessions brokerSessions() {
		return brokerSessions;
	}

	public SessionSealer sessionSealer() {
		return new FileSessionSealer(datalake, stageDirectory());
	}

	private void injectJmsConfiguration() {
		if (graph.datalake() != null) {
			graph.datalake().path(datalakeDirectory().getAbsolutePath());
			if (graph.datalake().backup() != null)
				graph.datalake().backup().path(configuration.backupDirectory().getAbsolutePath());
		}
		if (graph.broker() != null) {
			graph.broker().path(brokerDirectory().getAbsolutePath());
			graph.broker().port(Integer.parseInt(configuration.brokerPort()));
			graph.broker().secondaryPort(Integer.parseInt(configuration.brokerSecondaryPort()));
		}
	}

	private File brokerDirectory() {
		return new File(configuration.home(), "datahub/broker");
	}

	public File stageDirectory() {
		return new File(configuration.home(), "datahub/stage");
	}

	public File mappersDirectory() {
		File mappers = new File(configuration.home(), "datahub/mappers");
		mappers.mkdirs();
		return mappers;
	}

	public SessionSealer sessionSealer(File stageDirectory) {
		return new FileSessionSealer(datalake, stageDirectory);
	}

	public Master master() {
		return master;
	}

	public void beforeStart() {
		stageDirectory().mkdirs();
		loadBrokerService();
		if (graph.datalake() != null) {
			this.datalake = new FileDatalake(datalakeDirectory());
			initMaster();
		}
		if (graph.broker() != null) {
			configureBroker();
			nessService = new NessService(this);
		}
		sentinels = new Sentinels(this);
	}

	private void initMaster() {
		master = new Master(getMasterConfig());
		master.start();
	}

	private Master.Config getMasterConfig() {
		Master.Config config = new Master.Config();
		config.datalakeRootPath(datalakeDirectory());
		config.serializer(MasterSerializers.getOrDefault(configuration.masterSerializer()));
		config.tripletsDigester(new DatahubTripletDigesterFactory().create());
		config.tripletsLoader(new DatahubEntityLoader(datalake.entityStore()));
		return config;
	}

	private File datalakeDirectory() {
		return new File(configuration.home(), "datalake");
	}

	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {

	}

	@Override
	protected AuthService authService(URL authServiceUrl) {
		return null;
	}

	private void loadBrokerService() {
		if (this.graph.broker() != null && graph.broker().implementation() == null)
			graph.broker().implementation(() -> new JmsBrokerService(graph, brokerStage(), datalake(), master));
	}

	private void configureBroker() {
		brokerService = graph.broker().implementation().get();
		this.brokerSessions = new BrokerSessions(brokerStage(), stageDirectory());
		try {
			brokerService.start();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private File brokerStage() {
		return new File(brokerDirectory(), "stage");
	}

	public FileDatalake datalake() {
		return datalake;
	}

	public void lastSeal(Instant now) {
		this.lastSeal = now;
	}

	public Instant lastSeal() {
		return lastSeal;
	}

	private static class DatahubEntityLoader implements EntityLoader {

		private final Datalake.EntityStore store;

		public DatahubEntityLoader(Datalake.EntityStore store) {
			this.store = store;
		}

		@Override
		public Stream<Triplet> loadTriplets(Stats stats) {
			return store.tanks()
					.peek(t -> stats.increment("Tanks read"))
					.flatMap(Datalake.EntityStore.Tank::tubs)
					.flatMap(tub -> readTripletsFrom(tub, stats));
		}

		private Stream<Triplet> readTripletsFrom(Datalake.EntityStore.Tub tub, Stats stats) {
			stats.increment(Stats.FILES_READ);
			return tub.triplets()
					.map(t -> new Triplet(t.subject(), t.verb(), t.object()))
					.peek(t -> stats.increment(Stats.TRIPLETS_READ));
		}
	}

	private class DatahubTripletDigesterFactory {

		private MasterTripletsDigester create() {
			List<Entity> typesWithComponents = typesWithComponents();
			if (typesWithComponents.isEmpty()) return MasterTripletsDigester.createDefault();
			return new ComponentsTripletsDigester(
					componentsByEntityType(typesWithComponents),
					typesWithComponents.stream().map(this::subjectType).collect(Collectors.toSet()),
					componentTypes().stream().map(this::subjectType).collect(Collectors.toSet())
			);
		}

		private String subjectType(Entity entity) {
			return entity.name$().toLowerCase();
		}

		private Map<String, List<ComponentAttributeDefinition>> componentsByEntityType(List<Entity> typesWithComponents) {
			Map<String, List<ComponentAttributeDefinition>> componentsByEntityType = new HashMap<>();
			for (Entity entity : typesWithComponents) {
				List<ComponentAttributeDefinition> definitions = getComponentsOf(entity)
						.map(c -> new ComponentAttributeDefinition(
								c.name$(),
								c.asEntity().name$(),
								type(c.core$().layerList())
						)).collect(Collectors.toList());

				componentsByEntityType.put(entity.name$(), definitions);
			}
			return componentsByEntityType;
		}

		private ComponentAttributeDefinition.Type type(List<String> types) {
			if (types.stream().anyMatch(t -> t.endsWith("$List"))) return ComponentAttributeDefinition.Type.List;
			if (types.stream().anyMatch(t -> t.endsWith("$Map"))) return ComponentAttributeDefinition.Type.Map;
			return ComponentAttributeDefinition.Type.Reference;
		}

		private Stream<Entity.Attribute> getComponentsOf(Entity e) {
			return e.attributeList().stream().filter(EntityData::isEntity).filter(a -> isComponent(a.asEntity().entity()));
		}

		private List<Entity> typesWithComponents() {
			return graph.entityList().stream()
					.filter(e -> !isComponent(e))
					.filter(this::hasComponents)
					.collect(Collectors.toList());
		}

		private List<Entity> componentTypes() {
			return graph.entityList().stream()
					.filter(this::isComponent)
					.collect(Collectors.toList());
		}

		private boolean hasComponents(Entity entity) {
			return entity.attributeList().stream().anyMatch(a -> a.isEntity() && isComponent(a.asEntity().entity()));
		}

		private boolean isComponent(Entity entity) {
			return entity.isComponent();
		}
	}
}