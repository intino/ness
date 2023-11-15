package io.intino.ness.datahubterminalplugin.datamarts.nodes;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TimelineNodeImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("timelineNode", "default")), (trigger("nodeimpl"))).output(literal("private class TimelineNodeImpl implements TimelineNode {\n\tprivate final String id;\n\tprivate final String type;\n\tprivate volatile File file;\n\tprivate volatile boolean hasDirectAccessToFile;\n\tprivate volatile io.intino.sumus.chronos.TimelineStore timelineFile;\n\tprivate volatile ChangeListener listener;\n\tprivate volatile boolean disposed;\n\n\tprivate TimelineNodeImpl(String id, String type) {\n\t\tthis(id, type, null);\n\t}\n\n\tprivate TimelineNodeImpl(String id, String type, File file) {\n\t\tthis.id = requireNonNull(id);\n\t\tthis.type = requireNonNull(type);\n\t\tthis.file = file;\n\t\tthis.hasDirectAccessToFile = file != null;\n\t}\n\n\t@Override\n\tpublic String id() {\n\t\treturn id;\n\t}\n\n\t@Override\n\tpublic String type() {\n\t\treturn type;\n\t}\n\n\t@Override\n    public boolean exists() {\n    \ttry {\n    \t\treturn timelineFile() != null;\n    \t} catch(TimelineNotAvailableException e) {\n    \t\treturn false;\n    \t}\n    }\n\n\t@Override\n\tpublic void dispose() {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\tclearCache();\n\t\t\tlistener = null;\n\t\t\ttimelines.remove(id + \":\" + type);\n\t\t\tdisposed = true;\n\t\t}\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.TimelineStore.TimeModel timeModel() throws TimelineNotAvailableException {\n\t\treturn timelineFile().timeModel();\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.TimelineStore.SensorModel sensorModel() throws TimelineNotAvailableException {\n\t\treturn timelineFile().sensorModel();\n\t}\n\n\t@Override\n\tpublic Instant first() throws TimelineNotAvailableException {\n\t\treturn timelineFile().first();\n\t}\n\n\t@Override\n\tpublic Instant last() throws TimelineNotAvailableException {\n\t\treturn timelineFile().last();\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.Timeline get() throws TimelineNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new TimelineNotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\treturn timelineFile().timeline();\n\t\t\t} catch(Exception e) {\n\t\t\t\tthrow new TimelineNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore timelineFile() throws TimelineNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new TimelineNotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\tif (hasDirectAccessToFile && file != null && file.exists()) return loadFile();\n\t\t\t\tif (timelineFile != null) return timelineFile;\n\t\t\t\treturn timelineFile = downloadFromDatahub();\n\t\t\t} catch(Exception e) {\n\t\t\t\tthrow new TimelineNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\t@Override\n\tpublic void setChangeListener(ChangeListener listener) {\n\t\tthis.listener = listener;\n\t}\n\n\tprivate void notifyChange() {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\ttry {\n\t\t\t\tclearCache();\n\t\t\t\tif (listener != null) new Thread(() -> listener.notifyChange(this), \"")).output(mark("datamart", "FirstUpperCase")).output(literal("-TimelineNodeImpl-\" + System.currentTimeMillis()).start();\n\t\t\t} catch(Throwable ignored) {}\n\t\t}\n\t}\n\n\tprivate void clearCache() {\n\t\ttimelineFile = null;\n\t\tif(!hasDirectAccessToFile) file = null;\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore loadFile() throws Exception {\n\t\treturn io.intino.sumus.chronos.TimelineStore.of(file);\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore downloadFromDatahub() throws Exception {\n\t\tjavax.jms.Message response = requestResponseFromDatahub(\"get-timeline=\" + id(), request(hasDirectAccessToFile ? \"path\" : \"download\"));\n\t\tif (!response.getBooleanProperty(\"success\")) throw new TimelineNotAvailableException(\"Could not get timeline \" + id + \" because datahub returned success=false in the response\");;\n\t\tif (response instanceof javax.jms.TextMessage textResponse) {\n\t\t\tfile = getFile(textResponse);\n\t\t\thasDirectAccessToFile = true;\n\t\t\tif (file != null && file.exists()) return loadFile();\n\t\t\tfile = null;\n\t\t\thasDirectAccessToFile = false;\n\t\t\tresponse = requestResponseFromDatahub(\"get-timeline=\" + id(), request(\"download\"));\n\t\t}\n\t\tif (!response.getBooleanProperty(\"success\")) throw new TimelineNotAvailableException(\"Could not get timeline \" + id + \" because datahub returned success=false in the response\");;\n\t\treturn readFromBytes((javax.jms.BytesMessage) response);\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore readFromBytes(javax.jms.BytesMessage m) throws Exception {\n\t\tint size = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[size];\n\t\tm.readBytes(bytes, size);\n\n\t\tfile = File.createTempFile(id(), \".timeline\");\n\t\tjava.nio.file.Files.write(file.toPath(), bytes, java.nio.file.StandardOpenOption.CREATE);\n\t\tfile.deleteOnExit();\n\t\thasDirectAccessToFile = false;\n\n\t\treturn loadFile();\n\t}\n\n\tprivate File getFile(javax.jms.TextMessage m) {\n\t\ttry {\n\t\t\treturn new File(m.getText());\n\t\t} catch(Exception e) {\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tprivate javax.jms.Message request(String mode) throws Exception {\n\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\tString command = \"datamart=\" + name() + \";operation=get-timeline;id=\" + id() + \";mode=\" + mode + \";type=\" + type;\n\t\tmessage.setText(command);\n\t\treturn message;\n\t}\n}"))
		);
	}
}