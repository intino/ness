package io.intino.ness.terminal.builder.codegeneration.datamarts.nodes;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class IndicatorImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("indicatorNode", "default")), (trigger("nodeimpl"))).output(literal("private class IndicatorNodeImpl implements IndicatorNode {\n\tprivate final String id;\n\tprivate volatile File file;\n\tprivate volatile boolean hasDirectAccessToFile;\n\n\tprivate IndicatorNodeImpl(String id, File file) {\n\t\tthis.id = requireNonNull(id);\n\t\tthis.file = file;\n\t\tthis.hasDirectAccessToFile = file != null;\n\t}\n\n\t@Override\n\tpublic boolean exists() {\n\t\ttry {\n\t\t\treturn get() != null;\n\t\t} catch(IndicatorNotAvailableException e) {\n\t\t\treturn false;\n\t\t}\n\t}\n\n\t@Override\n\tpublic Indicator get() throws IndicatorNotAvailableException {\n\t\tsynchronized(this) {\n\t\t\ttry {\n\t\t\t\tif (hasDirectAccessToFile) return Indicator.load(new java.io.FileInputStream(this.file));\n\t\t\t\treturn downloadFromDatahub();\n\t\t\t} catch(Exception e) {\n\t\t\t\tthrow new IndicatorNotAvailableException(e);\n\t\t\t}\n\t\t}\n\t}\n\n\n\tprivate Indicator downloadFromDatahub() throws Exception {\n\t\tjakarta.jms.Message response = requestResponseFromDatahub(\"get-indicator=\" + id, request(\"download\"));\n\t\tif (!response.getBooleanProperty(\"success\")) throw new IndicatorNotAvailableException(\"Could not get indicator \" + id + \" because datahub returned success=false in the response\");\n\t\treturn readFromBytes((jakarta.jms.BytesMessage) response);\n\t}\n\n\tprivate Indicator readFromBytes(jakarta.jms.BytesMessage m) throws Exception {\n\t\tint messageSize = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[messageSize];\n\t\tm.readBytes(bytes, messageSize);\n\t\treturn Indicator.load(new ByteArrayInputStream(bytes));\n\t}\n\n\tprivate jakarta.jms.Message request(String mode) throws Exception {\n\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\tmessage.setText(\"datamart=\" + name() + \";operation=get-indicator;id=\" + id + \";mode=\" + mode);\n\t\treturn message;\n\t}\n}"))
		);
	}
}