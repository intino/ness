package io.intino.datahub.box.service.jms;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.model.Attribute;
import io.intino.datahub.model.Component;
import io.intino.datahub.model.Datalake;
import jakarta.jms.Message;
import org.apache.activemq.command.ActiveMQTextMessage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetamodelRequest {
	private final DataHubBox box;

	public MetamodelRequest(DataHubBox box) {
		this.box = box;
	}

	public Stream<Message> accept(Message request) {
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setBooleanProperty("success", true);
			message.setText(calculateResponse());
			return Stream.of(message);
		} catch (Throwable e) {
			Logger.error(e);
			return Stream.of();
		}
	}

	public String calculateResponse() {
		var messages = box.graph().datalake().tankList(Datalake.Tank::isMessage).stream().map(t -> t.asMessage().message()).toList();
		JsonArray elements = new JsonArray();
		for (io.intino.datahub.model.Message message : messages) {
			JsonObject obj = new JsonObject();
			if (message.isAssertion()) obj.add("assertion", new JsonPrimitive(true));
			obj.add("name", new JsonPrimitive(message.name$()));
			obj.add("attributes", attributes(message));
			if (message.isExtensionOf()) obj.add("hierarchy", hierarchy(message));
			JsonArray components = components(message);
			if (!components.isEmpty()) obj.add("components", components);
			elements.add(obj);
		}
		return Json.toJson(elements);
	}

	private JsonElement hierarchy(io.intino.datahub.model.Message message) {
		var current = message;
		JsonArray hierarchy = new JsonArray();
		while (current.isExtensionOf()) {
			io.intino.datahub.model.Message parent = current.asExtensionOf().parent();
			hierarchy.add(new JsonPrimitive(parent.name$()));
			current = parent;
		}
		return hierarchy;
	}

	private JsonArray attributes(io.intino.datahub.model.Message message) {
		JsonArray attributes = new JsonArray();
		attributes.addAll(attributes(message.attributeList()));
		if (message.isExtensionOf())
			attributes.addAll(attributes(message.asExtensionOf().parent()));
		return attributes;

	}

	private JsonArray attributes(List<Attribute> attributeList) {
		List<JsonObject> attributes = attributeList.stream().map(this::map).toList();
		JsonArray jsonElements = new JsonArray();
		attributes.forEach(jsonElements::add);
		return jsonElements;
	}

	private JsonArray components(io.intino.datahub.model.Message message) {
		JsonArray components = new JsonArray();
		components.addAll(components(message.componentList()));
		if (message.isExtensionOf())
			components.addAll(components(message.asExtensionOf().parent()));
		return components;
	}

	private JsonObject map(Attribute a) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("name", new JsonPrimitive(a.name$()));
		jsonObject.add("type", new JsonPrimitive(a.asType().type()));
		return jsonObject;
	}

	private JsonArray components(Component owner) {
		JsonArray components = new JsonArray();
		components.addAll(components(owner.componentList()));
		if (owner.isExtensionOf())
			components.addAll(components(owner.asExtensionOf().parent()));
		components.addAll(components(owner.hasList().stream().map(Component.Has::element).collect(Collectors.toList())));
		return components;
	}

	private JsonArray components(List<Component> components) {
		JsonArray jsonElements = new JsonArray();
		components.stream().map(this::map).forEach(jsonElements::add);
		return jsonElements;
	}

	private JsonObject map(Component component) {
		JsonObject obj = new JsonObject();
		obj.add("name", new JsonPrimitive(component.name$()));
		obj.add("multiple", new JsonPrimitive(component.multiple()));
		obj.add("attributes", attributes(component));
		obj.add("components", components(component));
		return obj;
	}

	private JsonArray attributes(Component component) {
		JsonArray attributes = new JsonArray();
		attributes.addAll(attributes(component.attributeList()));
		if (component.isExtensionOf())
			attributes.addAll(attributes(component.asExtensionOf().parent()));
		return attributes;
	}
}
