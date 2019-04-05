import io.intino.ness.core.Datalake;
import io.intino.ness.datalake.hadoop.HadoopDatalake;
import org.junit.Test;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

public class HadoopDatalake_ {


	@Test
	public void should_write_messages_in_sequence_file() throws IOException, URISyntaxException, LoginException {
		HadoopDatalake datalake = new HadoopDatalake("hdfs://10.10.215.43:9000", "hadoop", "monentia");
		Datalake.EventStore.Tank tank = datalake.eventStore().tank("DA.FacturacionServicio");
		Stream<Datalake.EventStore.Tank> tanks = datalake.eventStore().tanks();
		tanks.forEach(t -> System.out.println(t.name()));
		if (tank != null) datalake.push(new LocalStage(new File("/Volumes/Untitled/sessions/Facturacion")).blobs());
	}

	@Test
	public void should_write_a_set_in_sequence_file() {

	}
}
