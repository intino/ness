import io.intino.ness.datalake.NessFunctionContainer;
import io.intino.ness.datalake.filesystem.FileDataLake;
import org.junit.Before;
import org.junit.Test;
import functions.*;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NessFunctionContainer_ {
    private NessFunctionContainer container;

    @Before
    public void setUp() throws Exception {
        container = new NessFunctionContainer(new FileDataLake("local"));
    }

    @Test
    public void should_start_with_no_plugs() throws Exception {
        assertThat(container.plugs(), is(""));
    }

    @Test
    public void should_plug_function() throws Exception {
        container.plug(TransformCallFunction.class).to("feed.test.text");
        assertThat(container.plugs(), is("feed.test.text > functions.TransformCallFunction : Never executed\n"));
    }

    @Test
    public void should_plug_function_from_code() throws Exception {
        container.plug("tests.UpperCaseFunction", NessCompiler_.upperCaseFunction()).to("feed.test.text");
        assertThat(container.plugs(), is("feed.test.text > tests.UpperCaseFunction : Never executed\n"));
    }


    @Test
    public void should_pump_topic() throws Exception {
        String topic = "feed.ultra.Call";
        container.plug(TransformCallFunction.class).to(topic);
        container.pump(topic);
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread == Thread.currentThread()) continue;
            thread.join();
        }
    }

    @Test
    public void should_pump_topic_with_one_csv() throws Exception {
        String topic = "legacy.edf.PowerConsumption";
        container.plug(ImportPowerConsumptionFunction.class).to(topic);
        Thread thread = container.pump(topic);
        thread.join();
    }

    @Test
    public void should_pump_topic_with_many_csv() throws Exception {
        String topic = "legacy.fracfocus.Job";
        container.plug(ImportFracWellFunction.class).to(topic);
        Thread thread = container.pump(topic);
        thread.join();
    }

}
