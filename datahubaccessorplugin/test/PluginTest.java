import io.intino.legio.graph.LegioGraph;
import io.intino.ness.datahubaccessorplugin.DataHubAccessorsPluginLauncher;
import io.intino.plugin.PluginLauncher;
import io.intino.tara.magritte.Graph;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

public class PluginTest {
	@Test
	@Ignore
	public void should_build_accessors() {
		DataHubAccessorsPluginLauncher launcher = new DataHubAccessorsPluginLauncher();
		launcher.
				moduleStructure(new PluginLauncher.ModuleStructure(Collections.singletonList(new File(System.getProperty("user.home") + "/workspace/ness/data-hub-test/src")), Collections.singletonList(new File(System.getProperty("user.home") + "/workspace/ness/data-hub-test/res")), new File("/Users/oroncal/workspace/ness/out/data-hub-test/")))
				.systemProperties(new PluginLauncher.SystemProperties(new File("/Applications/IntelliJ IDEA - 2019.2.app/Contents/plugins/maven/lib/maven3/"), new File("/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home")))
				.logger(System.out)
				.moduleConfiguration(new Graph().loadStashes("data-hub-test").as(LegioGraph.class));
		launcher.run();
	}
}
