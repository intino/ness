package io.intino.ness.datahubterminalplugin;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class PomTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("pom"))).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n\n<groupId>")).output(mark("group", "lowercase")).output(literal("</groupId>\n<artifactId>")).output(mark("artifact", "lowercase")).output(literal("</artifactId>\n<version>")).output(mark("version")).output(literal("</version>\n")).output(expression().output(literal("<licenses>")).output(literal("\n")).output(literal("\t")).output(mark("license").multiple("\n")).output(literal("\n")).output(literal("</licenses>"))).output(literal("\n<properties>\n\t<maven.compiler.source>11</maven.compiler.source>\n\t<maven.compiler.target>11</maven.compiler.target>\n</properties>\n\n<build>\n\t<sourceDirectory>src</sourceDirectory>\n\t<outputDirectory>out/production/")).output(mark("artifact", "lowercase")).output(literal("</outputDirectory>\n\t<testOutputDirectory>out/test/")).output(mark("artifact", "lowercase")).output(literal("</testOutputDirectory>\n\t<directory>out/build/")).output(mark("artifact", "lowercase")).output(literal("</directory>\n\t<resources>\n\t\t<resource>\n\t\t\t<directory>${basedir}/res</directory>\n\t\t</resource>\n\t</resources>\n\t<plugins>\n\t\t<plugin>\n\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t<version>3.0.1</version>\n\t\t\t<executions>\n\t\t\t\t<execution>\n\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t<goals>\n\t\t\t\t\t\t<goal>jar-no-fork</goal>\n\t\t\t\t\t</goals>\n\t\t\t\t</execution>\n\t\t\t</executions>\n\t\t</plugin>\n\t\t<plugin>\n\t\t\t<groupId>org.codehaus.mojo</groupId>\n\t\t\t<artifactId>build-helper-maven-plugin</artifactId>\n\t\t\t<version>3.2.0</version>\n\t\t\t<executions>\n\t\t\t\t<execution>\n\t\t\t\t\t<id>add-source</id>\n\t\t\t\t\t<phase>generate-sources</phase>\n\t\t\t\t\t<goals>\n\t\t\t\t\t\t<goal>add-source</goal>\n\t\t\t\t\t</goals>\n\t\t\t\t\t<configuration>\n\t\t\t\t\t\t<sources>\n\t\t\t\t\t\t\t")).output(mark("sourceDirectory").multiple("\n")).output(literal("\n\t\t\t\t\t\t</sources>\n\t\t\t\t\t</configuration>\n\t\t\t\t</execution>\n\t\t\t</executions>\n\t\t</plugin>\n\t</plugins>\n</build>\n\n<repositories>\n\t")).output(mark("repository", "release").multiple("\n")).output(literal("\n\t<repository>\n\t\t<id>intino-maven</id>\n\t\t<name>intino-maven-releases</name>\n\t\t<url>https://artifactory.intino.io/artifactory/releases</url>\n\t</repository>\n</repositories>\n\n<distributionManagement>\n\t")).output(mark("repository", "distribution").multiple("\n")).output(literal("\n</distributionManagement>\n\n<dependencies>\n\t")).output(expression().output(mark("event"))).output(literal("\n\t")).output(expression().output(mark("led"))).output(literal("\n\t")).output(expression().output(mark("terminal"))).output(literal("\n\t")).output(expression().output(mark("bpm"))).output(literal("\n</dependencies>\n</project>")),
			rule().condition((trigger("event"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>event</artifactId>\n\t<version>")).output(mark("version")).output(literal("</version>\n</dependency>")),
			rule().condition((trigger("led"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>led</artifactId>\n\t<version>1.1.1</version>\n</dependency>")),
			rule().condition((trigger("terminal"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>terminal-jms</artifactId>\n\t<version>")).output(mark("terminalVersion")).output(literal("</version>\n</dependency>\n<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>ingestion</artifactId>\n\t<version>")).output(mark("ingestionVersion")).output(literal("</version>\n</dependency>\n<dependency>\n\t<groupId>")).output(mark("group", "lowercase")).output(literal("</groupId>\n\t<artifactId>")).output(mark("artifact", "lowercase")).output(literal("</artifactId>\n\t<version>")).output(mark("version")).output(literal("</version>\n</dependency>")),
			rule().condition((allTypes("repository","distribution")), (trigger("distribution"))).output(literal("<repository>\n\t<id>")).output(mark("name")).output(literal("</id>\n\t<name>")).output(mark("name")).output(literal("</name>\n\t<url>")).output(mark("url")).output(literal("</url>\n</repository>")),
			rule().condition((trigger("distribution"))),
			rule().condition((trigger("bpm"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>bpm-framework</artifactId>\n\t<version>")).output(mark("")).output(literal("</version>\n\t<scope>provided</scope>\n</dependency>")),
			rule().condition((type("repository")), not(type("distribution")), (trigger("release"))).output(literal("<repository>\n\t<id>")).output(mark("name")).output(literal("-")).output(mark("random")).output(literal("</id>\n\t<url>")).output(mark("url")).output(literal("</url>\n</repository>")),
			rule().condition((type("GPL")), (trigger("license"))).output(literal("<license>\n\t<name>The GNU General Public License v3.0</name>\n\t<url>https://www.gnu.org/licenses/gpl-3.0.txt</url>\n</license>")),
			rule().condition((type("BSD")), (trigger("license"))).output(literal("<license>\n\t<name>BSD 3-Clause License</name>\n\t<url>https://opensource.org/licenses/BSD-3-Clause</url>\n</license>")),
			rule().condition((trigger("sourcedirectory"))).output(literal("<source>")).output(mark("")).output(literal("</source>"))
		);
	}
}