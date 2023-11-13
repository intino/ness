package io.intino.ness.datahubterminalplugin.datamarts.nodes;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class IndicatorImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("indicatorNode", "default")), (trigger("nodeimpl"))).output(literal("private class IndicatorNodeImpl implements IndicatorNode {\n\tprivate final String id;\n\tprivate volatile File file;\n\tprivate volatile boolean hasDirectAccessToFile;\n\n\tprivate IndicatorNodeImpl(String id, File file) {\n\t\tthis.id = requireNonNull(id);\n\t\tthis.file = file;\n\t\tthis.hasDirectAccessToFile = file != null;\n\t}\n\n\t@Override\n\tpublic Indicator get() throws Indicator NotAvailableException {\n\t\tsynchronized(this) {\n\t\t\tif (disposed) throw new Indicator NotAvailableException(\"This \" + getClass().getSimpleName() + \" is disposed.\");\n\t\t\ttry {\n\t\t\t\tif (hasDirectAccessToFile) return loadFile(this.file);\n\t\t\t\treturn downloadFromDatahub();\n\t\t\t} catch(Exception e) {\n\t\t\t\tthrow new Indicator NotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\tpublic Indicator loadFile(File file) throws IOException {\n\t\tMap<String, Shot> shots;\n\t\tif (!file.exists()) return new Indicator(new HashMap<>());\n\t\ttry (var stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {\n\t\t\tint size = stream.readInt();\n\t\t\tshots = new HashMap<>(size);\n\t\t\tfor (int i = 0; i < size; i++)\n\t\t\t\tshots.put(stream.readUTF(), new Shot(ofEpochMilli(stream.readLong()), stream.readDouble()));\n\t\t}\n\t\treturn new Indicator(shots);\n\t}\n\n\tprivate Datamart.Indicator downloadFromDatahub() throws Exception {\n\t\tresponse = requestResponseFromDatahub(\"get-indicator=\" + id(), request(\"download\"));\n\t\tif (!response.getBooleanProperty(\"success\")) throw new IndicatorNotAvailableException(\"Could not get indicator \" + id + \" because datahub returned success=false in the response\");\n\t\treturn readFromBytes((javax.jms.BytesMessage) response);\n\t}\n\n\tprivate Datamart.Indicator readFromBytes(javax.jms.BytesMessage m) throws Exception {\n\t\tint size = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[size];\n\t\tm.readBytes(bytes, size);\n\t\tMap<String, Shot> shots;\n\t\ttry (var stream = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bytes))))) {\n\t\t\tint size = stream.readInt();\n\t\t\tshots = new HashMap<>(size);\n\t\t\tfor (int i = 0; i < size; i++)\n\t\t\t\tshots.put(stream.readUTF(), new Indicator.Shot(ofEpochMilli(stream.readLong()), stream.readDouble()));\n\t\t}\n\t\treturn new Indicator(shots);\n\t}\n}"))
		);
	}
}