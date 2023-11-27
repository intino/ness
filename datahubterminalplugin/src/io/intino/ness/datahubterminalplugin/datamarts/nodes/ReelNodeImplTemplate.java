package io.intino.ness.datahubterminalplugin.datamarts.nodes;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ReelNodeImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("reelNode", "default")), (trigger("nodeimpl"))).output(literal("private class ReelNodeImpl implements ReelNode {\n\tprivate final String id;\n\tprivate final String type;\n\tprivate volatile File file;\n\tprivate volatile boolean hasDirectAccessToFile;\n\tprivate volatile io.intino.sumus.chronos.ReelFile reelFile;\n\tprivate volatile ChangeListener listener;\n\tprivate volatile boolean disposed;\n\n\tprivate ReelNodeImpl(String id, String type) {\n\t\tthis(id, type, null);\n\t}\n\n\tprivate ReelNodeImpl(String id, String type, File file) {\n\t\tthis.id = requireNonNull(id);\n\t\tthis.type = requireNonNull(type);\n\t\tthis.file = file;\n\t\tthis.hasDirectAccessToFile = file != null;\n\t}\n\n\t@Override\n\tpublic String id() {\n\t\treturn id;\n\t}\n\n\t@Override\n\tpublic String type() {\n\t\treturn type;\n\t}\n\n\t@Override\n\tpublic void dispose() {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\tclearCache();\n\t\t\tlistener = null;\n\t\t\treels.remove(id + \":\" + type);\n\t\t\tdisposed = true;\n\t\t}\n\t}\n\n\t@Override\n\tpublic java.time.Instant start() throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\tif (reelFile == null) return null;\n\t\treturn reelFile.start();\n\t}\n\n\t@Override\n\tpublic io.intino.sumus.chronos.State stateOf(String signal) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.lastStateOf(signal);\n\t}\n\n\t@Override\n\tpublic java.util.Set<String> signals() throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.signals();\n\t}\n\n\t@Override\n\tpublic List<io.intino.sumus.chronos.State> stateOf(Stream<String> signals) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn signals.map(reelFile::lastStateOf).toList();\n\t}\n\n\tpublic io.intino.sumus.chronos.Shot lastShotOf(String signal) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.lastShotOf(signal);\n\t}\n\n\tpublic List<io.intino.sumus.chronos.Shot> lastShots() throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.lastShots();\n\t}\n\n\tpublic List<io.intino.sumus.chronos.Shot> lastShots(String group) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.lastShots(group);\n\t}\n\n\tpublic List<io.intino.sumus.chronos.Shot> lastShots(io.intino.sumus.chronos.Group group) throws ReelNotAvailableException {\n\t\tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n\t\treturn reelFile.lastShots(group);\n\t}\n\n\t@Override\n    public io.intino.sumus.chronos.Reel get(io.intino.sumus.chronos.Period period) throws ReelNotAvailableException {\n    \tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n    \treturn reelFile().reel().by(period);\n    }\n\n\t@Override\n    public io.intino.sumus.chronos.Reel get(Instant from, Instant to, io.intino.sumus.chronos.Period period) throws ReelNotAvailableException {\n    \tio.intino.sumus.chronos.ReelFile reelFile = reelFile();\n    \treturn reelFile.reel(from, to).by(period);\n    }\n\n\t@Override\n    public boolean exists() {\n    \ttry {\n    \t\treturn reelFile() != null;\n    \t} catch(ReelNotAvailableException e) {\n    \t\treturn false;\n    \t}\n    }\n\n\tprivate io.intino.sumus.chronos.ReelFile reelFile() throws ReelNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new ReelNotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\tif (hasDirectAccessToFile && file != null && file.exists()) return loadFile();\n\t\t\t\tif (reelFile != null) return reelFile;\n\t\t\t\treturn reelFile = downloadFromDatahub();\n\t\t\t} catch (Exception e) {\n\t\t\t\tthrow new ReelNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\t@Override\n\tpublic void setChangeListener(ChangeListener listener) {\n\t\tthis.listener = listener;\n\t}\n\n\tprivate void notifyChange() {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) return;\n\t\t\ttry {\n\t\t\t\tclearCache();\n\t\t\t\tif (listener != null) new Thread(() -> listener.notifyChange(this), \"")).output(mark("datamart", "FirstUpperCase")).output(literal("-ReelNodeImpl-\" + System.currentTimeMillis()).start();\n\t\t\t} catch(Throwable ignored) {}\n\t\t}\n\t}\n\n\tprivate void clearCache() {\n\t\treelFile = null;\n\t\tif(!hasDirectAccessToFile) file = null;\n\t}\n\n\tprivate io.intino.sumus.chronos.ReelFile loadFile() throws Exception {\n\t\treturn io.intino.sumus.chronos.ReelFile.open(file);\n\t}\n\n\tprivate io.intino.sumus.chronos.ReelFile downloadFromDatahub() throws Exception {\n\t\tjakarta.jms.Message response = requestResponseFromDatahub(\"get-reel=\" + id(), request(hasDirectAccessToFile ? \"path\" : \"download\"));\n\t\tif (!response.getBooleanProperty(\"success\")) throw new ReelNotAvailableException(\"Could not get reel \" + id + \" because datahub returned success=false in the response\");\n\t\tif (response instanceof jakarta.jms.TextMessage textResponse) {\n\t\t\tfile = getFile(textResponse);\n\t\t\thasDirectAccessToFile = true;\n\t\t\tif (file != null && file.exists()) return loadFile();\n\t\t\tfile = null;\n\t\t\thasDirectAccessToFile = false;\n\t\t\tresponse = requestResponseFromDatahub(\"get-reel=\" + id(), request(\"download\"));\n\t\t}\n\t\tif (!response.getBooleanProperty(\"success\")) throw new ReelNotAvailableException(\"Could not get reel \" + id + \" because datahub returned success=false in the response\");\n\t\treturn readFromBytes((jakarta.jms.BytesMessage) response);\n\t}\n\n\tprivate io.intino.sumus.chronos.ReelFile readFromBytes(jakarta.jms.BytesMessage m) throws Exception {\n\t\tint size = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[size];\n\t\tm.readBytes(bytes, size);\n\t\tfile = File.createTempFile(id(), \".reel\");\n\t\tjava.nio.file.Files.write(file.toPath(), bytes, java.nio.file.StandardOpenOption.CREATE);\n\t\tfile.deleteOnExit();\n\t\thasDirectAccessToFile = false;\n\t\treturn loadFile();\n\t}\n\n\tprivate File getFile(jakarta.jms.TextMessage m) {\n\t\ttry {\n\t\t\treturn new File(m.getText());\n\t\t} catch(Exception e) {\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tprivate jakarta.jms.Message request(String mode) throws Exception {\n\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\tString command = \"datamart=\" + name() + \";operation=get-reel;id=\" + id() + \";mode=\" + mode + \";type=\" + type;\n\t\tmessage.setText(command);\n\t\treturn message;\n\t}\n}"))
		);
	}
}