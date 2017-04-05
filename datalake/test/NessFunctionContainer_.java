import com.sun.javafx.geom.transform.Identity;
import io.intino.ness.datalake.NessFunctionContainer;
import io.intino.ness.datalake.filesystem.FileDataLake;
import io.intino.ness.inl.Inl;
import io.intino.ness.inl.Message;
import org.junit.Before;
import org.junit.Test;
import functions.*;

import java.time.Instant;
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
    public void should_pump_topic() throws Exception {
        Thread thread = container
                .pump("feed.ultra.Call")
                .with(TransformCallFunction.class)
                .into("feed.ultra.Call.1");
        thread.join();
    }

    @Test
    public void should_pump_topic_with_one_csv_file() throws Exception {
        Thread thread = container
                .pump("legacy.edf.PowerConsumption")
                .with(ImportPowerConsumptionFunction.class)
                .into("feed.edf.PowerConsumption.1");
        thread.join();
    }

    @Test
    public void should_pump_topic_with_many_csv_files() throws Exception {
        Thread thread = container
                .pump("legacy.fracfocus.Job")
                .with(ImportFracWellFunction.class)
                .into("feed.fracfocus.Job.1");
        thread.join();
    }

    @Test
    public void should_pump_many_topics_with_dat_file() throws Exception {
        Thread thread = container
                .pump("legacy.ritheim.Heater")
                .with(ImportHeaterFunction.class)
                .into("feed.ritheim.Heater.1");
        thread.join();
    }

}
