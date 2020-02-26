package io.intino.test;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.Connection;
import javax.jms.MessageNotWriteableException;
import javax.jms.Session;
import java.time.Instant;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class CFEProducer {
	private final String url = "tcp://localhost:63000";
	private final String user = "contratacion";
	private final String password = "Qu5cFKTu51fd";
	private final String topic = "contratacion.Contrato";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;

	public CFEProducer() {
		initSession();
	}

	public static void main(String[] args) throws MessageNotWriteableException {
//		new CFEProducer().produceMessage();
		System.out.println(message().toString());
	}

	public void produceMessage() throws MessageNotWriteableException {
		final Message contrato = message();
		topicProducer.produce(createMessage(contrato.toString()));

	}

	private static Message message() {
		final Message contrato = new Message("Contrato").
				set("ts", Instant.now().toString()).
				set("id", "f4e66534-52ee-470d-92f1-64e317f745be").
				set("codigo", "K70").
				set("nombre", "COMAPA DE EL MANTE").
				set("división", "DU").
				set("tipo", "CuentaMaestra").
				set("estado", "Borrador").
				set("alias", "").
				set("giro", "").
				set("ss", "").
				set("rfc", "");
		Message convenioCuentaMaestra = new Message("ConvenioCuentaMaestra");
		convenioCuentaMaestra.set("morosidadDiasVencimiento", "10").
				set("morosidadInteresDiferencial", "3").
				set("facturacionTipoCobranza", "").
				set("facturacionAdmiteCentavos", "false").
				set("pagoReferenciaBancaria", "").
				set("pagoCuentaBancaria", "");
		Message direccion = new Message("Direccion");
		direccion.set("calle", "").
				set("numero", "").
				set("numeroInterior", "").
				set("colonia", "").
				set("localidad", "").
				set("municipio", "").
				set("estado", "").
				set("pais", "").
				set("codigoPostal", "");
		contrato.add(convenioCuentaMaestra);
		contrato.add(direccion);
		return contrato;
	}

	private ActiveMQTextMessage createMessage(String message) throws MessageNotWriteableException {
		ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
		textMessage.setText(message);
		return textMessage;
	}

	private void initSession() {
		try {
			this.connection = makeConnection(user, password, url);
			this.connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			this.topicProducer = new TopicProducer(session, topic);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
