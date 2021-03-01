package io.intino.ness.datahubterminalplugin.lookups;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class LookupTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\nimport io.intino.alexandria.logger.Logger;\n\nimport java.sql.*;\n\n")).output(mark("lookup")),
			rule().condition((allTypes("lookup","dynamic"))).output(literal("public class ")).output(mark("name", "FirstUpperCase")).output(literal(" implements ")).output(mark("rootPackage")).output(literal(".DynamicLookup {\n\tprivate static final Entry NA = new Entry(")).output(mark("column", "defaultValue").multiple(", ")).output(literal(");\n\tprivate final java.sql.Connection connection;\n\tprivate Statement getStatement;\n\tprivate Statement setStatement;\n\tprivate int count = 0;\n\t")).output(expression().output(literal("private final java.util.Set<")).output(mark("idColumnType", "FirstUpperCase")).output(literal("> ")).output(mark("idColumnName")).output(literal(";"))).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(java.sql.Connection connection) {\n\t\tthis.connection = connection;\n\t\t")).output(expression().output(literal("this.")).output(mark("idColumnName")).output(literal(" = new java.util.HashSet<>();"))).output(literal("\n\t}\n\n\tpublic void open() {\n\t\ttry {\n\t\t\tthis.connection.createStatement().execute(\"CREATE TABLE IF NOT EXISTS ")).output(mark("name", "FirstUpperCase")).output(literal(" (")).output(mark("column", "createSql").multiple(", ")).output(literal(");\");\n\t\t\t")).output(expression().output(mark("index", "createOnOpen").multiple("\n"))).output(literal("\n\t\t\tthis.getStatement = connection.createStatement();\n\t\t\tthis.setStatement = connection.createStatement();\n\t\t} catch (SQLException throwables) {\n\t\t\tLogger.error(throwables);\n\t\t}\n\t}\n\n\tpublic void put(")).output(mark("column", "parameter").multiple(", ")).output(literal(") {\n\t\ttry {\n\t\t\tsetStatement.addBatch(\"INSERT INTO ")).output(mark("name", "FirstUpperCase")).output(literal(" (")).output(mark("column", "name").multiple(", ")).output(literal(") VALUES(\" + ")).output(mark("column", "sqlValue").multiple(" + \", \" + ")).output(literal(" + \");\");\n\t\t\t")).output(expression().output(literal("this.")).output(mark("idColumnName")).output(literal(".add(")).output(mark("idColumnName")).output(literal(");"))).output(literal("\n\t\t\texecuteStatement();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\t")).output(expression().output(mark("index", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("column", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("column", "setter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("index", "delete").multiple("\n\n"))).output(literal("\n\n\tpublic void delete(")).output(mark("column", "parameter").multiple(", ")).output(literal(") {\n\t\ttry {\n\t\t\tString textStatement = \"DELETE FROM ")).output(mark("name", "FirstUpperCase")).output(literal(" WHERE \";\n\t\t\t")).output(mark("column", "whereFilter").multiple(" + \" AND \";\n")).output(literal(";\n\t\t\tsetStatement.addBatch(textStatement);\n\t\t\texecuteStatement();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\tpublic void commit() {\n\t\ttry {\n\t\t\tsetStatement.executeBatch();\n\t\t\tconnection.commit();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\tpublic void close() {\n\t\ttry {\n\t\t\tif (connection.isClosed()) return;\n\t\t\t")).output(expression().output(mark("index", "createOnClose").multiple("\n"))).output(literal("\n\t\t\tcommit();\n\t\t\tconnection.close();\n\t\t} catch (SQLException e) {\n\t\t\tLogger.error(e);\n\t\t}\n\t}\n\n\tprivate void executeStatement() throws SQLException {\n\t\tif (++count % 100_000 != 0) return;\n\t\tsetStatement.executeBatch();\n\t}\n\n\tprivate ResultSet query(String query) throws SQLException {\n\t\treturn getStatement.executeQuery(query);\n\t}\n\n\t")).output(mark("index", "putEmpty")).output(literal("\n\n\tpublic static class Entry {\n\t\t")).output(mark("column", "declaration").multiple("\n")).output(literal("\n\n\t\tEntry(")).output(mark("column", "parameter").multiple(", ")).output(literal(") {\n\t\t\t")).output(mark("column", "assign").multiple("\n")).output(literal("\n\t\t}\n\t}\n}")),
			rule().condition(not(type("id")), (trigger("navalue"))).output(literal("NA.")).output(mark("name", "firstLowerCase")),
			rule().condition((type("text")), (trigger("wherefilter"))).output(literal("if (")).output(mark("name", "firstLowerCase")).output(literal(" != null) textStatement += \"")).output(mark("name", "firstLowerCase")).output(literal("='\" + ")).output(mark("name", "firstLowerCase")).output(literal(" + \"'\"")),
			rule().condition((trigger("wherefilter"))).output(literal("if (")).output(mark("name", "firstLowerCase")).output(literal(" != null) textStatement += \"")).output(mark("name", "firstLowerCase")).output(literal("=\" + ")).output(mark("name", "firstLowerCase")),
			rule().condition((allTypes("column","id")), (trigger("createsql"))).output(mark("name", "firstLowerCase")).output(literal(" ")).output(mark("type", "sqlType")).output(literal(" NOT NULL PRIMARY KEY")),
			rule().condition((allTypes("column","category")), (trigger("createsql"))).output(mark("name", "firstLowerCase")).output(literal(" int")).output(expression().output(literal(" ")).output(mark("isRequired"))),
			rule().condition((type("column")), (trigger("createsql"))).output(mark("name", "firstLowerCase")).output(literal(" ")).output(mark("type", "sqlType")).output(expression().output(literal(" ")).output(mark("isRequired"))),
			rule().condition((attribute("", "true")), (trigger("isrequired"))).output(literal("NOT NULL")),
			rule().condition((allTypes("column","id")), (trigger("idname"))).output(mark("name", "firstLowerCase")),
			rule().condition((trigger("idname"))),
			rule().condition((type("column")), (trigger("name"))).output(mark("name", "firstLowerCase")),
			rule().condition((allTypes("column","id")), (trigger("idtype"))).output(mark("type", "FirstUpperCase")),
			rule().condition((type("column")), (trigger("idtype"))),
			rule().condition((type("column")), (trigger("defaultvalue"))).output(mark("defaultValue")),
			rule().condition((type("column")), (type("text")), (trigger("sqlvalue"))).output(literal("\"'\" + ")).output(mark("name", "firstLowerCase")).output(literal(" + \"'\"")),
			rule().condition((type("column")), (type("date")), (trigger("sqlvalue"))).output(mark("name", "firstLowerCase")).output(literal(".toEpochDay()")),
			rule().condition((type("column")), (type("datetime")), (trigger("sqlvalue"))).output(mark("name", "firstLowerCase")).output(literal(".toEpochMilli()")),
			rule().condition((type("column")), (type("category")), (trigger("sqlvalue"))).output(mark("name", "firstLowerCase")).output(literal(".index")),
			rule().condition((type("column")), (trigger("sqlvalue"))).output(mark("name", "firstLowerCase")),
			rule().condition((type("column")), (attribute("type", "int")), (trigger("parameter"))).output(literal("Integer ")).output(mark("name", "firstLowerCase")),
			rule().condition((type("column")), (anyTypes("date","datetime")), (trigger("parameter"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")),
			rule().condition((type("column")), (trigger("parameter"))).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")),
			rule().condition((type("column")), not(type("id")), (type("category")), (trigger("getresult"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("lookup")).output(literal(".entry(rs.getInt(")).output(mark("index")).output(literal("));")),
			rule().condition((type("column")), (type("date")), (trigger("getresult"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = java.time.LocalDate.ofEpochDay(rs.getLong(")).output(mark("index")).output(literal("));")),
			rule().condition((type("column")), (type("datetime")), (trigger("getresult"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = java.time.Instant.ofEpochMilli(rs.getLong(")).output(mark("index")).output(literal("));")),
			rule().condition((type("column")), not(type("id")), (trigger("getresult"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = rs.get")).output(mark("type", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(");")),
			rule().condition((attribute("", "long")), (trigger("sqltype"))).output(literal("bigint")),
			rule().condition((attribute("", "java.time.LocalDate")), (trigger("sqltype"))).output(literal("bigint")),
			rule().condition((attribute("", "java.time.Instant")), (trigger("sqltype"))).output(literal("bigint")),
			rule().condition((attribute("", "string")), (trigger("sqltype"))).output(literal("text")),
			rule().condition((type("column")), (trigger("assign"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("column")), (trigger("declaration"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("onOpen")), (trigger("createonopen"))).output(literal("this.connection.createStatement().execute(\"CREATE INDEX IF NOT EXISTS idx_")).output(mark("name")).output(literal(" ON ")).output(mark("table", "FirstUpperCase")).output(literal(" (")).output(mark("idxColumn", "name").multiple(", ")).output(literal(");\");")),
			rule().condition((type("onClose")), (trigger("createonclose"))).output(literal("this.connection.createStatement().execute(\"CREATE INDEX IF NOT EXISTS idx_")).output(mark("name")).output(literal(" ON ")).output(mark("table", "FirstUpperCase")).output(literal(" (")).output(mark("idxColumn", "name").multiple(", ")).output(literal(");\");")),
			rule().condition((type("text")), (trigger("wherefiltervalue"))).output(literal("\"")).output(mark("name", "firstLowerCase")).output(literal("='\" + ")).output(mark("name", "firstLowerCase")).output(literal(" + \"'\"")),
			rule().condition((trigger("wherefiltervalue"))).output(literal("\"")).output(mark("name", "firstLowerCase")).output(literal("=\" + ")).output(mark("name", "firstLowerCase")),
			rule().condition((type("column")), not(type("idx")), (trigger("localfield"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("column")), not(type("idx")), (trigger("getresultlist"))).output(mark("name", "firstLowerCase")).output(literal(" = rs.get")).output(mark("type", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(");")),
			rule().condition((allTypes("index","id")), (trigger("getter"))).output(literal("public Entry entry(")).output(mark("idxColumn", "parameter").multiple(", ")).output(literal(") {\n\ttry {\n\t\tResultSet rs = query(\"SELECT * FROM ")).output(mark("name", "FirstUpperCase")).output(literal(" WHERE ")).output(mark("column", "idName")).output(literal("=\" + ")).output(mark("column", "idName")).output(literal(" + \";\");\n\t\tboolean next = rs.next();\n\t\tif (!next) return NA;\n\t\t")).output(mark("column", "getResult").multiple("\n")).output(literal("\n\t\treturn new Entry(")).output(mark("column", "name").multiple(", ")).output(literal(");\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t\treturn NA;\n\t}\n}")),
			rule().condition((type("index")), (trigger("getter"))).output(literal("public java.util.List<Entry> entriesBy")).output(mark("name", "FirstUpperCase")).output(literal("(")).output(mark("idxColumn", "parameter").multiple(", ")).output(literal(") {\n\tjava.util.List<Entry> result = new java.util.ArrayList<>();\n\t")).output(mark("column", "localField").multiple("\n")).output(literal("\n\ttry {\n\t\tResultSet rs = query(\"SELECT * FROM ")).output(mark("table", "FirstUpperCase")).output(literal(" WHERE \" + ")).output(mark("idxColumn", "whereFilterValue").multiple(" + ")).output(literal(" + \";\");\n\t\twhile (rs.next()) {\n\t\t\t")).output(mark("column", "getResultList").multiple("\n")).output(literal("\n\t\t\tresult.add(new Entry(")).output(mark("column", "name").multiple(", ")).output(literal("));\n\t\t}\n\t\treturn result;\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t\treturn result;\n\t}\n}")),
			rule().condition((allTypes("index","id")), (trigger("putempty"))).output(literal("private void putEmpty(")).output(mark("idxColumn", "parameter")).output(literal(") {\n\tput(")).output(mark("name", "firstLowerCase")).output(literal(", ")).output(mark("column", "naValue").multiple(",")).output(literal(");\n}")),
			rule().condition((allTypes("column","text")), not(type("id")), (type("hasId")), (trigger("setter"))).output(literal("public void ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("idColumnType")).output(literal(" ")).output(mark("idColumnName", "firstLowerCase")).output(literal(", ")).output(mark("type")).output(literal(" value) {\n\ttry {\n\t\tif (!this.")).output(mark("idColumnName")).output(literal(".contains(")).output(mark("idColumnName", "firstLowerCase")).output(literal(")) putEmpty(")).output(mark("idColumnName", "firstLowerCase")).output(literal(");\n\t\tthis.setStatement.addBatch(\"UPDATE ")).output(mark("table", "FirstUpperCase")).output(literal(" SET ")).output(mark("name", "firstLowerCase")).output(literal("='\" + value + \"' WHERE ")).output(mark("idColumnName", "firstLowerCase")).output(literal("='\" +  ")).output(mark("idColumnName", "firstLowerCas")).output(literal(" + \"';\");\n\t\texecuteStatement();\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t}\n}")),
			rule().condition((allTypes("column","category")), not(type("id")), (type("hasId")), (trigger("setter"))).output(literal("public void ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("idColumnType")).output(literal(" ")).output(mark("idColumnName", "firstLowerCase")).output(literal(", ")).output(mark("type")).output(literal(" value) {\n\ttry {\n\t\tthis.setStatement.addBatch(\"UPDATE ")).output(mark("table", "FirstUpperCase")).output(literal(" SET ")).output(mark("name", "firstLowerCase")).output(literal("=\" + value.index + \" WHERE ")).output(mark("idColumnName", "firstLowerCase")).output(literal("=\" +  ")).output(mark("idColumnName", "firstLowerCas")).output(literal(" +\";\");\n\t\texecuteStatement();\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t}\n}")),
			rule().condition((type("column")), not(type("id")), (type("hasId")), (trigger("setter"))).output(literal("public void ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("idColumnType")).output(literal(" ")).output(mark("idColumnName", "firstLowerCase")).output(literal(", ")).output(mark("type")).output(literal(" value) {\n\ttry {\n\t\tthis.setStatement.addBatch(\"UPDATE ")).output(mark("table", "FirstUpperCase")).output(literal(" SET ")).output(mark("name", "firstLowerCase")).output(literal("=\" + value + \" WHERE ")).output(mark("idColumnName", "firstLowerCase")).output(literal("=\" + ")).output(mark("idColumnName", "firstLowerCase")).output(literal(" +\";\");\n\t\texecuteStatement();\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t}\n}")),
			rule().condition((type("column")), not(type("id")), (type("hasId")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("idColumnType")).output(literal(" ")).output(mark("idColumnName", "firstLowerCase")).output(literal(") {\n\treturn entry(")).output(mark("idColumnName", "firstLowerCase")).output(literal(").")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("column")), (type("id")), (trigger("delete"))).output(literal("public void delete(")).output(mark("type")).output(literal(" ")).output(mark("name")).output(literal(") {\n\ttry {\n\t\tsetStatement.addBatch(\"DELETE FROM ")).output(mark("table", "FirstUpperCase")).output(literal(" WHERE ")).output(mark("name", "idName")).output(literal("=\" + ")).output(mark("name", "sqlValue")).output(literal(");\n\t\texecuteStatement();\n\t} catch (SQLException e) {\n\t\tLogger.error(e);\n\t}\n}")),
			rule().condition((allTypes("column","category")), (trigger("insert"))).output(literal("setStatement.set")).output(mark("typePrimitive", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(".index);")),
			rule().condition((type("column")), (trigger("insert"))).output(literal("setStatement.set")).output(mark("typePrimitive", "FirstUpperCase")).output(literal("(")).output(mark("index")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");")),
			rule().condition((trigger("emptyend"))).output(literal(",")),
			rule().condition((trigger("empty"))).output(literal(","))
		);
	}
}