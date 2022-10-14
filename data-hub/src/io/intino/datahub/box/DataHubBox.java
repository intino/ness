package io.intino.datahub.box;

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
import io.intino.magritte.framework.Node;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.data.ComponentAttributeDefinition;
import io.intino.ness.master.data.ComponentsDatalakeLoader;
import io.intino.ness.master.data.DatalakeLoader;
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

	public void beforeStart() {
		stageDirectory().mkdirs();
		load();
		if (graph.datalake() != null) this.datalake = new FileDatalake(datalakeDirectory());
		if (graph.broker() != null) {
			configureBroker();
			nessService = new NessService(this);
		}
		initMaster();
		sentinels = new Sentinels(this);
	}

	private void initMaster() {
		master = new Master(getMasterConfig());
		master.start();
	}

	private Master.Config getMasterConfig() {
		Master.Config config = new Master.Config();
		config.datalakeRootPath(datalakeDirectory());
		config.instanceName(configuration.masterInstanceName());
		config.host(configuration.masterHost());
		config.port(Integer.parseInt(configuration.masterPort()));
		config.serializer(MasterSerializers.getOrDefault(configuration.masterSerializer()));
		config.datalakeLoader(new DatalakeLoaderFactory().create());
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

	private void load() {
		if (this.graph.broker() != null && graph.broker().implementation() == null)
			graph.broker().implementation(() -> new JmsBrokerService(graph, brokerStage()));
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

	private class DatalakeLoaderFactory {

		// TODO: test
		private DatalakeLoader create() {
			List<Entity> typesWithComponents = typesWithComponents();
			if(typesWithComponents.isEmpty()) return DatalakeLoader.createDefault();
			return new ComponentsDatalakeLoader(
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
			for(Entity entity : typesWithComponents) {
				List<ComponentAttributeDefinition> definitions = getComponentsOf(entity)
						.map(c -> new ComponentAttributeDefinition(
								c.name$(),
								c.asEntity().name$(),
								type(c.asEntity().type())
						)).collect(Collectors.toList());

				componentsByEntityType.put(entity.name$(), definitions);
			}
			return componentsByEntityType;
		}

		private ComponentAttributeDefinition.Type type(String type) {
			if(type.contains("List")) return ComponentAttributeDefinition.Type.List;
			if(type.contains("Map")) return ComponentAttributeDefinition.Type.Map;
			return ComponentAttributeDefinition.Type.Reference;
		}

		private Stream<Entity.Attribute> getComponentsOf(Entity e) {
			return e.attributeList().stream().filter(EntityData::isEntity).filter(a -> isComponent(a.asEntity().core$()));
		}

		private List<Entity> typesWithComponents() {
			return graph.entityList().stream()
					.filter(e -> !isComponent(e.core$()))
					.filter(this::hasComponents)
					.collect(Collectors.toList());
		}

		private List<Entity> componentTypes() {
			return graph.entityList().stream()
					.filter(e -> isComponent(e.core$()))
					.collect(Collectors.toList());
		}

		private boolean hasComponents(Entity entity) {
			return entity.attributeList().stream().anyMatch(a -> a.isEntity() && isComponent(a.core$()));
		}

		private boolean isComponent(Node entity) {
			return entity.conceptList().stream().anyMatch(c -> c.isAspect && c.name().contains("Component"));
		}
	}
}