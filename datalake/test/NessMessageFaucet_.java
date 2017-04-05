import functions.ImportHeaterFunction;
import io.intino.ness.datalake.NessMessageFaucet;
import io.intino.ness.datalake.filesystem.FileTopic;
import io.intino.ness.datalake.virtual.VirtualTopic;
import io.intino.ness.inl.Message;
import org.junit.Test;

import java.io.File;

import static io.intino.ness.datalake.NessDataLake.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NessMessageFaucet_ {

    @Test
    public void name() throws Exception {
        Topic topic = new VirtualTopic(new FileTopic(new File("local/legacy.ritheim.Heater")), new ImportHeaterFunction());
        NessMessageFaucet faucet = new NessMessageFaucet(topic);
        String last = "";
        while (true) {
            Message message = faucet.next();
            if (message == null) return;
            String ts = message.read("ts").as(String.class);
            assertThat(last.compareTo(ts) <= 0, is(true));
            last = ts;
        }
    }

}
