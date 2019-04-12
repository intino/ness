import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.hadoop.HadoopConnection;
import io.intino.ness.datalake.hadoop.HadoopDatalake;
import io.intino.ness.datalake.hadoop.HadoopSessionManager;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.util.stream.Stream;

public class HadoopDatalake_ {


	@Test
	public void should_write_messages_in_sequence_file() {
		HadoopConnection connection = new HadoopConnection("hdfs://10.10.215.43:9000", "hadoop", "monentia");
		connection.connect();
		HadoopDatalake datalake = new HadoopDatalake(connection.fs());
		Datalake.EventStore.Tank tank = datalake.eventStore().tank("DA.FacturacionServicio");
		Stream<Datalake.EventStore.Tank> tanks = datalake.eventStore().tanks();
		tanks.forEach(t -> System.out.println(t.name()));
		if (tank != null) {
			HadoopSessionManager hadoopSessionManager = new HadoopSessionManager(datalake, connection.fs(), new Path(connection.fs().getWorkingDirectory(), "sessions"));
//			hadoopDigester.push(new FileStage(new File("/Volumes/Untitled/sessions/Facturacion", )).blobs());TODO
		}
	}

	@Test
	public void should_write_a_set_in_sequence_file() {

	}
}
