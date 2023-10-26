package io.intino.ness.datahubterminalplugin.datamarts;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class NodeImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("timelineNode","default")), (trigger("nodeimpl"))).output(literal("\nprivate class TimelineNodeImpl implements TimelineNode {\n\n\tprivate final String id;\n\tprivate final String type;\n\tprivate final Set<String> sources;\n\tprivate volatile File file;\n\tprivate volatile boolean hasDirectAccessToFile;\n\tprivate volatile io.intino.sumus.chronos.TimelineStore timelineFile;\n\tprivate volatile EventListener listener;\n\tprivate volatile boolean disposed;\n\n\tprivate TimelineNodeImpl(String id, String type, Set<String> sources) {\n\t\tthis(id, type, sources, null);\n\t}\n\n\tprivate TimelineNodeImpl(String id, String type, Set<String> sources, File file) {\n\t\tthis.id = requireNonNull(id);\n\t\tthis.type = requireNonNull(type);\n\t\tthis.sources = requireNonNull(sources);\n\t\tthis.file = file;\n\t\tthis.hasDirectAccessToFile = file != null;\n\t}\n\n\t@Override\n\tpublic String id() {\n\t\treturn id;\n\t}\n\n\t@Override\n\tpublic String type() {\n\t\treturn type;\n\t}\n\n\t@Override\n    public boolean exists() {\n    \ttry {\n    \t\treturn timelineFile() != null;\n    \t} catch(TimelineNotAvailableException e) {\n    \t\treturn false;\n    \t}\n    }\n\n\t@Override\n\tpublic void dispose() {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\tclearCache();\n\t\t\tlistener = null;\n\t\t\ttimelines.remove(id + \":\" + type);\n\t\t\tdisposed = true;\n\t\t}\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.TimelineStore.TimeModel timeModel() throws TimelineNotAvailableException {\n\t\treturn timelineFile().timeModel();\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.TimelineStore.SensorModel sensorModel() throws TimelineNotAvailableException {\n\t\treturn timelineFile().sensorModel();\n\t}\n\n\t@Override\n\tpublic Instant first() throws TimelineNotAvailableException {\n\t\treturn timelineFile().first();\n\t}\n\n\t@Override\n\tpublic Instant last() throws TimelineNotAvailableException {\n\t\treturn timelineFile().last();\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.Timeline get() throws TimelineNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new TimelineNotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\treturn timelineFile().timeline();\n\t\t\t} catch(Exception e) {\n\t\t\t\tthrow new TimelineNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore timelineFile() throws TimelineNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new TimelineNotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\tif (hasDirectAccessToFile && file != null && file.exists()) return loadFile();\n\t\t\t\tif (timelineFile != null) return timelineFile;\n\t\t\t\treturn timelineFile = downloadFromDatahub();\n\t\t\t} catch(Exception e) {\n\t\t\t\tthrow new TimelineNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\t@Override\n\tpublic void setEventListener(EventListener listener) {\n\t\tthis.listener = listener;\n\t}\n\n\tprivate void notifyEvent(io.intino.alexandria.event.Event event) {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\ttry {\n\t\t\t\tif (!sources.contains(event.type())) return;\n\t\t\t\tclearCache();\n\t\t\t\tif (listener != null) listener.onEventReceived(this, event);\n\t\t\t} catch(Throwable ignored) {}\n\t\t}\n\t}\n\n\tprivate void clearCache() {\n\t\ttimelineFile = null;\n\t\tif(!hasDirectAccessToFile) file = null;\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore loadFile() throws Exception {\n\t\treturn io.intino.sumus.chronos.TimelineStore.of(file);\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore downloadFromDatahub() throws Exception {\n\t\tjavax.jms.Message response = requestResponseFromDatahub(\n\t\t\t\"get-timeline=\" + id(),\n\t\t \trequest(TimelineNode.AlwaysDownloadFromDatahub.get() ? \"download\" : \"path\"));\n\t\tif (!response.getBooleanProperty(\"success\")) throw new TimelineNotAvailableException(\"Could not get timeline \" + id + \" because datahub returned success=false in the response\");;\n\t\tif (response instanceof javax.jms.TextMessage textResponse) {\n\t\t\tfile = getFile(textResponse);\n\t\t\thasDirectAccessToFile = true;\n\t\t\tif (file != null && file.exists()) return loadFile();\n\t\t\tfile = null;\n\t\t\thasDirectAccessToFile = false;\n\t\t\tresponse = requestResponseFromDatahub(\"get-timeline=\" + id(), request(\"download\"));\n\t\t}\n\t\tif (!response.getBooleanProperty(\"success\")) throw new TimelineNotAvailableException(\"Could not get timeline \" + id + \" because datahub returned success=false in the response\");;\n\t\treturn readFromBytes((javax.jms.BytesMessage) response);\n\t}\n\n\tprivate io.intino.sumus.chronos.TimelineStore readFromBytes(javax.jms.BytesMessage m) throws Exception {\n\t\tint size = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[size];\n\t\tm.readBytes(bytes, size);\n\n\t\tfile = File.createTempFile(id(), \".timeline\");\n\t\tjava.nio.file.Files.write(file.toPath(), bytes, java.nio.file.StandardOpenOption.CREATE);\n\t\tfile.deleteOnExit();\n\t\thasDirectAccessToFile = false;\n\n\t\treturn loadFile();\n\t}\n\n\tprivate File getFile(javax.jms.TextMessage m) {\n\t\ttry {\n\t\t\treturn new File(m.getText());\n\t\t} catch(Exception e) {\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tprivate javax.jms.Message request(String mode) throws Exception {\n\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\tString command = \"datamart=\" + name() + \";operation=get-timeline;id=\" + id() + \";mode=\" + mode + \";type=\" + type;\n\t\tmessage.setText(command);\n\t\treturn message;\n\t}\n}")),
			rule().condition((allTypes("reelNode","default")), (trigger("nodeimpl"))).output(literal("\nprivate class ReelNodeImpl implements ReelNode {\n\tprivate final String id;\n\tprivate final String type;\n\tprivate final Set<String> sources;\n\tprivate volatile File file;\n\tprivate volatile boolean hasDirectAccessToFile;\n\tprivate volatile io.intino.sumus.chronos.ReelFile reelFile;\n\tprivate volatile EventListener listener;\n\tprivate volatile boolean disposed;\n\n\tprivate ReelNodeImpl(String id, String type, Set<String> sources) {\n\t\tthis(id, type, sources, null);\n\t}\n\n\tprivate ReelNodeImpl(String id, String type, Set<String> sources, File file) {\n\t\tthis.id = requireNonNull(id);\n\t\tthis.type = requireNonNull(type);\n\t\tthis.sources = requireNonNull(sources);\n\t\tthis.file = file;\n\t\tthis.hasDirectAccessToFile = file != null;\n\t}\n\n\t@Override\n\tpublic String id() {\n\t\treturn id;\n\t}\n\n\t@Override\n\tpublic String type() {\n\t\treturn type;\n\t}\n\n\t@Override\n\tpublic void dispose() {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\tclearCache();\n\t\t\tlistener = null;\n\t\t\treels.remove(id + \":\" + type);\n\t\t\tdisposed = true;\n\t\t}\n\t}\n\n\t@Override\n\tpublic java.time.Instant start() throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\tif (reelFile == null) return null;\n\t\treturn reelFile.start();\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.Reel.State stateOf(String signal) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.stateOf(signal);\n\t}\n\n\t@Override\n\tpublic List<io.intino.sumus.chronos.Reel.State> stateOf(Stream<String> signals) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn signals.map(reelFile::stateOf).toList();\n\t}\n\n\t@Override\n    public io.intino.sumus.chronos.Reel get(io.intino.sumus.chronos.Period period) throws ReelNotAvailableException {\n    \tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n    \treturn reelFile().reel().by(period);\n    }\n\n\t@Override\n    public io.intino.sumus.chronos.Reel get(Instant from, Instant to, io.intino.sumus.chronos.Period period) throws ReelNotAvailableException {\n    \tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n    \treturn reelFile.reel(from, to).by(period);\n    }\n\n\t@Override\n    public boolean exists() {\n    \ttry {\n    \t\treturn reelFile() != null;\n    \t} catch(ReelNotAvailableException e) {\n    \t\treturn false;\n    \t}\n    }\n\n\tprivate io.intino.sumus.chronos.ReelFile reelFile() throws ReelNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new ReelNotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\tif (hasDirectAccessToFile && file != null && file.exists()) return loadFile();\n\t\t\t\tif (reelFile != null) return reelFile;\n\t\t\t\treturn reelFile = downloadFromDatahub();\n\t\t\t} catch (Exception e) {\n\t\t\t\tthrow new ReelNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\t@Override\n\tpublic void setEventListener(EventListener listener) {\n\t\tthis.listener = listener;\n\t}\n\n\tprivate void notifyEvent(io.intino.alexandria.event.Event event) {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\ttry {\n\t\t\t\tif (!sources.contains(event.type())) return;\n\t\t\t\tclearCache();\n\t\t\t\tif (listener != null) listener.onEventReceived(this, event);\n\t\t\t} catch(Throwable ignored) {}\n\t\t}\n\t}\n\n\tprivate void clearCache() {\n\t\treelFile = null;\n\t\tif(!hasDirectAccessToFile) file = null;\n\t}\n\n\tprivate io.intino.sumus.chronos.ReelFile loadFile() throws Exception {\n\t\treturn io.intino.sumus.chronos.ReelFile.open(file);\n\t}\n\n\tprivate io.intino.sumus.chronos.ReelFile downloadFromDatahub() throws Exception {\n\t\tjavax.jms.Message response = requestResponseFromDatahub(\"get-reel=\" + id(), request(\"path\"));\n\t\tif (!response.getBooleanProperty(\"success\")) throw new ReelNotAvailableException(\"Could not get reel \" + id + \" because datahub returned success=false in the response\");\n\t\tif (response instanceof javax.jms.TextMessage textResponse) {\n\t\t\tfile = getFile(textResponse);\n\t\t\thasDirectAccessToFile = true;\n\t\t\tif (file != null && file.exists()) return loadFile();\n\t\t\tfile = null;\n\t\t\thasDirectAccessToFile = false;\n\t\t\tresponse = requestResponseFromDatahub(\"get-reel=\" + id(), request(\"download\"));\n\t\t}\n\t\tif (!response.getBooleanProperty(\"success\")) throw new ReelNotAvailableException(\"Could not get reel \" + id + \" because datahub returned success=false in the response\");\n\t\treturn readFromBytes((javax.jms.BytesMessage) response);\n\t}\n\n\tprivate io.intino.sumus.chronos.ReelFile readFromBytes(javax.jms.BytesMessage m) throws Exception {\n\t\tint size = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[size];\n\t\tm.readBytes(bytes, size);\n\t\tfile = File.createTempFile(id(), \".reel\");\n\t\tjava.nio.file.Files.write(file.toPath(), bytes, java.nio.file.StandardOpenOption.CREATE);\n\t\tfile.deleteOnExit();\n\t\thasDirectAccessToFile = false;\n\t\treturn loadFile();\n\t}\n\n\tprivate File getFile(javax.jms.TextMessage m) {\n\t\ttry {\n\t\t\treturn new File(m.getText());\n\t\t} catch(Exception e) {\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tprivate javax.jms.Message request(String mode) throws Exception {\n\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\tString command = \"datamart=\" + name() + \";operation=get-reel;id=\" + id() + \";mode=\" + mode + \";type=\" + type;\n\t\tmessage.setText(command);\n\t\treturn message;\n\t}\n}"))
		);
	}
}