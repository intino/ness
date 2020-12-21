package io.intino.ness.datahubterminalplugin.renders.lookups;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DynamicLookupTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\nimport io.intino.alexandria.logger.Logger;\n\nimport java.sql.*;\n\n")).output(mark("lookup")),
			rule().condition((allTypes("lookup","dynamic"))).output(literal("public class ")).output(mark("name", "FirstUpperCase")).output(literal(" implements ")).output(mark("rootPackage")).output(literal(".DynamicLookup {\n\tprivate static final Entry NA = new Entry(")).output(mark("column", "defaultValue").multiple(", ")).output(literal(");\n\tprivate final java.sql.Connection connection;\n\tprivate PreparedStatement insertStatement;\n\tprivate PreparedStatement queryStatement;\n\t")).output(mark("column", "updateStatementField").multiple("\n")).output(literal("\n\tprivate int count = 0;\n\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(java.sql.Connection connection) {\n\t\tthis.connection = connection;\n\t}\n\n\tpublic void open() {\n\t\ttry {\n\t\t\tthis.connection.createStatement().execute(\"CREATE TABLE IF NOT EXISTS ")).output(mark("name", "FirstUpperCase")).output(literal(" (")).output(mark("column", "createSql").multiple(", ")).output(literal(");\");\n\t\t\t")).output(expression().output(mark("index", "createOnOpen").multiple("\n"))).output(literal("\n\t\t\tthis.insertStatement = connection.prepareStatement(\"INSERT INTO ")).output(mark("name", "FirstUpperCase")).output(literal(" (")).output(mark("column", "name").multiple(", ")).output(literal(") VALUES(")).output(mark("column", "question").multiple(", ")).output(literal(");\");\n\t\t\tthis.queryStatement = connection.prepareStatement(\"SELECT * FROM ")).output(mark("name", "FirstUpperCase")).output(literal(" WHERE ")).output(mark("column", "idName")).output(literal("=?;\");\n\t\t\t")).output(expression().output(mark("column", "updateStatement").multiple("\n"))).output(literal("\n\t\t} catch (SQLException throwables) {\n\t\t\tLogger.error(throwables);\n\t\t}\n\t}\n\n\tpublic Entry entry(")).output(mark("column", "idType")).output(literal(" ")).output(mark("column", "idName")).output(literal(") {\n\t\ttry {\n\t\t\tResultSet rs = query(")).output(mark("column", "idName")).output(literal(");\n\t\t\tboolean next = rs.next();\n\t\t\tif (!next) return NA;\n\t\t\t")).output(mark("column", "getResult").multiple("\n")).output(literal("\n\t\t\treturn new Entry(")).output(mark("column", "name").multiple(", ")).output(literal(");\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t\treturn NA;\n\t\t}\n\t}\n\n\tpublic void put(")).output(mark("column", "parameter").multiple(", ")).output(literal(") {\n\t\ttry {\n\t\t\tinsertStatement.clearParameters();\n\t\t\t")).output(mark("column", "insert").multiple("\n")).output(literal("\n\t\t\tinsertStatement.addBatch();\n\t\t\texecuteStatement();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\t")).output(expression().output(mark("column", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("column", "setter").multiple("\n\n"))).output(literal("\n\n\tpublic void commit() {\n\t\ttry {\n\t\t\tinsertStatement.executeBatch();\n\t\t\t")).output(mark("column", "updateStatementExecute").multiple("\n")).output(literal("\n\t\t\tconnection.commit();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\tpublic void close() {\n\t\ttry {\n\t\t\tif (connection.isClosed()) return;\n\t\t\t")).output(expression().output(mark("index", "createOnClose").multiple("\n"))).output(literal("\n\t\t\tcommit();\n\t\t\tconnection.close();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\tprivate void executeStatement() throws SQLException {\n\t\tif (++count % 100_000 != 0) return;\n\t\tinsertStatement.executeBatch();\n\t\t")).output(expression().output(mark("column", "updateStatementExecute").multiple("\n"))).output(literal("\n\t}\n\n\tprivate ResultSet query(")).output(mark("column", "idType")).output(literal(" index) throws SQLException {\n\t\tqueryStatement.clearParameters();\n\t\tqueryStatement.set")).output(mark("column", "idType")).output(literal("(1, index);\n\t\treturn queryStatement.executeQuery();\n\t}\n\n\tpublic static class Entry {\n\t\t")).output(mark("column", "declaration").multiple("\n")).output(literal("\n\n\t\tEntry(")).output(mark("column", "parameter").multiple(", ")).output(literal(") {\n\t\t\t")).output(mark("column", "assign").multiple("\n")).output(literal("\n\t\t}\n\t}\n}")),
			rule().condition((allTypes("column","id")), (trigger("createsql"))).output(mark("name", "firstLowerCase")).output(literal(" ")).output(mark("type", "sqlType")).output(literal(" NOT NULL PRIMARY_KEY")),
			rule().condition((type("column")), (trigger("createsql"))).output(mark("name", "firstLowerCase")).output(literal(" ")).output(mark("type", "sqlType")).output(expression().output(literal(" ")).output(mark("isRequired"))),
			rule().condition((attribute("", "true")), (trigger("isrequired"))).output(literal("NOT NULL")),
			rule().condition((allTypes("column","id")), (trigger("idname"))).output(mark("name", "firstLowerCase")),
			rule().condition((trigger("idname"))),
			rule().condition((type("column")), (trigger("name"))).output(mark("name", "firstLowerCase")),
			rule().condition((allTypes("column","id")), (trigger("idtype"))).output(mark("type", "FirstUpperCase")),
			rule().condition((type("column")), (trigger("idtype"))),
			rule().condition((type("column")), (trigger("defaultvalue"))).output(mark("defaultValue")),
			rule().condition((type("column")), (trigger("question"))).output(literal("?")),
			rule().condition((type("column")), not(type("id")), (trigger("updatestatementfield"))).output(literal("private PreparedStatement update")).output(mark("name", "FirstUpperCase")).output(literal("Statement;")),
			rule().condition((type("column")), not(type("id")), (trigger("updatestatementexecute"))).output(literal("update")).output(mark("name", "FirstUpperCase")).output(literal("Statement.execute();")),
			rule().condition((type("column")), not(type("id")), (trigger("updatestatement"))).output(literal("this.update")).output(mark("name", "FirstUpperCase")).output(literal("Statement = connection.prepareStatement(\"UPDATE ")).output(mark("table")).output(literal(" SET ")).output(mark("name", "firstLowerCase")).output(literal("=? WHERE ")).output(mark("idColumnName", "firstLowerCase")).output(literal("=?;\");")),
			rule().condition((type("column")), (trigger("parameter"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")),
			rule().condition((type("column")), not(type("id")), (type("category")), (trigger("getresult"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("lookup")).output(literal(".entry(rs.getInt(")).output(mark("index")).output(literal("));")),
			rule().condition((type("column")), not(type("id")), (trigger("getresult"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = rs.get")).output(mark("type", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(");")),
			rule().condition((attribute("", "boolean")), (trigger("sqltype"))).output(literal("int")),
			rule().condition((attribute("", "long")), (trigger("sqltype"))).output(literal("bigint")),
			rule().condition((attribute("", "string")), (trigger("sqltype"))).output(literal("text")),
			rule().condition((type("column")), (trigger("assign"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("column")), (trigger("declaration"))).output(literal("public final ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("onOpen")), (trigger("createonopen"))).output(literal("this.connection.createStatement().execute(\"CREATE INDEX IF NOT EXISTS ")).output(mark("name")).output(literal(" ON ")).output(mark("table")).output(literal(" (")).output(mark("column", "firstLowerCase").multiple(", ")).output(literal(");\");")),
			rule().condition((type("onClose")), (trigger("createonclose"))).output(literal("this.connection.createStatement().execute(\"CREATE INDEX IF NOT EXISTS ")).output(mark("name")).output(literal(" ON ")).output(mark("table")).output(literal(" (")).output(mark("column", "firstLowerCase").multiple(", ")).output(literal(");\");")),
			rule().condition((type("column")), not(type("id")), (trigger("setter"))).output(literal("public void ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("idColumnType")).output(literal(" ")).output(mark("idColumnName", "firstLowerCase")).output(literal(", ")).output(mark("type")).output(literal(" value) {\n\ttry {\n\t\tupdate")).output(mark("name", "FirstUpperCase")).output(literal("Statement.clearParameters();\n\t\tupdate")).output(mark("name", "FirstUpperCase")).output(literal("Statement.set")).output(mark("idColumnType", "firstUpperCase")).output(literal("(1, ")).output(mark("idColumnName", "firstLowerCase")).output(literal(");\n\t\tupdate")).output(mark("name", "FirstUpperCase")).output(literal("Statement.set")).output(mark("typePrimitive", "firstUpperCase")).output(literal("(2")).output(expression().output(mark("lookup", "empty")).output(literal(" value.index")).next(expression().output(literal(", value")))).output(literal(");\n\t\tupdate")).output(mark("name", "FirstUpperCase")).output(literal("Statement.addBatch();\n\t\texecuteStatement();\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t}\n}")),
			rule().condition((type("column")), not(type("id")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("idColumnType")).output(literal(" ")).output(mark("idColumnName", "firstLowerCase")).output(literal(") {\n\treturn entry(")).output(mark("idColumnName", "firstLowerCase")).output(literal(").")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((allTypes("column","category")), (trigger("insert"))).output(literal("insertStatement.set")).output(mark("typePrimitive", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(".index);")),
			rule().condition((type("column")), (trigger("insert"))).output(literal("insertStatement.set")).output(mark("typePrimitive", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");")),
			rule().condition((trigger("empty"))).output(literal(","))
		);
	}
}