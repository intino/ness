package systems.intino.eventsourcing.datahubbuilder.codegeneration;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class PomTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("pom")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n\n<groupId>")).output(placeholder("group", "lowercase")).output(literal("</groupId>\n<artifactId>")).output(placeholder("artifact", "lowercase")).output(literal("</artifactId>\n<version>")).output(placeholder("version")).output(literal("</version>\n")).output(expression().output(literal("<licenses")).output(literal(">")).output(literal("\n")).output(literal("\t")).output(placeholder("license").multiple("\n")).output(literal("\n")).output(literal("</licenses")).output(literal(">"))).output(literal("\n<properties>\n\t<maven.compiler.source>21</maven.compiler.source>\n    <maven.compiler.target>21</maven.compiler.target>\n\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n</properties>\n\n<build>\n\t<sourceDirectory>src</sourceDirectory>\n\t<outputDirectory>out/production/")).output(placeholder("artifact", "lowercase")).output(literal("</outputDirectory>\n\t<testOutputDirectory>out/test/")).output(placeholder("artifact", "lowercase")).output(literal("</testOutputDirectory>\n\t<directory>out/build/")).output(placeholder("artifact", "lowercase")).output(literal("</directory>\n\t<resources>\n\t\t<resource>\n\t\t\t<directory>${basedir}/res</directory>\n\t\t</resource>\n\t</resources>\n\t<plugins>\n\t\t<plugin>\n\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t<version>3.0.1</version>\n\t\t\t<executions>\n\t\t\t\t<execution>\n\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t<goals>\n\t\t\t\t\t\t<goal>jar</goal>\n\t\t\t\t\t</goals>\n\t\t\t\t</execution>\n\t\t\t</executions>\n\t\t</plugin>\n\t\t<plugin>\n\t\t\t<groupId>org.codehaus.mojo</groupId>\n\t\t\t<artifactId>build-helper-maven-plugin</artifactId>\n\t\t\t<version>3.5.0</version>\n\t\t\t<executions>\n\t\t\t\t<execution>\n\t\t\t\t\t<id>add-source</id>\n\t\t\t\t\t<phase>generate-sources</phase>\n\t\t\t\t\t<goals>\n\t\t\t\t\t\t<goal>add-source</goal>\n\t\t\t\t\t</goals>\n\t\t\t\t\t<configuration>\n\t\t\t\t\t\t<sources>\n\t\t\t\t\t\t\t")).output(placeholder("sourceDirectory").multiple("\n")).output(literal("\n\t\t\t\t\t\t</sources>\n\t\t\t\t\t</configuration>\n\t\t\t\t</execution>\n\t\t\t</executions>\n\t\t</plugin>\n\t</plugins>\n</build>\n\n<repositories>\n\t")).output(placeholder("repository", "release").multiple("\n")).output(literal("\n</repositories>\n\n")).output(expression().output(literal("<distributionManagement")).output(literal(">")).output(literal("\n")).output(literal("\t")).output(placeholder("repository", "distribution").multiple("\n")).output(literal("\n")).output(literal("</distributionManagement")).output(literal(">"))).output(literal("\n\n<dependencies>\n\t")).output(expression().output(placeholder("event"))).output(literal("\n\t")).output(expression().output(placeholder("terminal"))).output(literal("\n\t")).output(expression().output(placeholder("terminalontology"))).output(literal("\n\t")).output(expression().output(placeholder("bpm"))).output(literal("\n</dependencies>\n</project>")));
		rules.add(rule().condition(trigger("terminalontology")).output(literal("<dependency>\n  \t<groupId>systems.intino.eventsourcing</groupId>\n\t<artifactId>datahub-terminal</artifactId>\n  \t<version>")).output(placeholder("version")).output(literal("</version>\n</dependency>")));
		rules.add(rule().condition(trigger("event")).output(literal("<dependency>\n\t<groupId>systems.intino.eventsourcing</groupId>\n\t<artifactId>event</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n</dependency>\n")));
		rules.add(rule().condition(trigger("terminal")).output(literal("<dependency>\n\t<groupId>systems.intino.eventsourcing</groupId>\n\t<artifactId>ingestion</artifactId>\n\t<version>")).output(placeholder("ingestionVersion")).output(literal("</version>\n</dependency>\n<dependency>\n\t<groupId>")).output(placeholder("group", "lowercase")).output(literal("</groupId>\n\t<artifactId>")).output(placeholder("artifact", "lowercase")).output(literal("</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n</dependency>")));
		rules.add(rule().condition(all(allTypes("repository", "distribution"), trigger("distribution"))).output(literal("<repository>\n\t<id>")).output(placeholder("name")).output(literal("</id>\n\t<name>")).output(placeholder("name")).output(literal("</name>\n\t<url>")).output(placeholder("url")).output(literal("</url>\n</repository>")));
		rules.add(rule().condition(trigger("distribution")));
		rules.add(rule().condition(trigger("bpm")).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>bpm-framework</artifactId>\n\t<version>")).output(placeholder("")).output(literal("</version>\n\t<scope>provided</scope>\n</dependency>\n")));
		rules.add(rule().condition(all(all(allTypes("repository"), not(allTypes("distribution"))), trigger("release"))).output(literal("<repository>\n\t<id>")).output(placeholder("name")).output(literal("-")).output(placeholder("random")).output(literal("</id>\n\t<name>")).output(placeholder("name")).output(literal("</name>\n\t<url>")).output(placeholder("url")).output(literal("</url>\n\t")).output(expression().output(literal("<snapshots")).output(literal(">")).output(literal("<enabled")).output(literal(">")).output(placeholder("snapshot")).output(literal("</enabled")).output(literal(">")).output(literal("</snapshots")).output(literal(">"))).output(literal("\n</repository>\n")));
		rules.add(rule().condition(all(allTypes("GPL"), trigger("license"))).output(literal("<license>\n\t<name>The GNU General Public License v3.0</name>\n\t<url>https://www.gnu.org/licenses/gpl-3.0.txt</url>\n</license>")));
		rules.add(rule().condition(all(allTypes("BSD"), trigger("license"))).output(literal("<license>\n\t<name>BSD 3-Clause License</name>\n\t<url>https://opensource.org/licenses/BSD-3-Clause</url>\n</license>\n")));
		rules.add(rule().condition(trigger("sourcedirectory")).output(literal("<source>")).output(placeholder("")).output(literal("</source>")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}