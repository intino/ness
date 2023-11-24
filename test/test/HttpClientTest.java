import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class HttpClientTest {

	@Test
	@Ignore
	public void name() throws JMSException {
		ConnectionFactory cf = null;
		Connection con = null;
		try {
			// getting jms connection from the server and starting it.
			System.out.println("Please wait connecting...");
			cf = new ActiveMQConnectionFactory("http://localhost:64000");
			con = cf.createConnection("user1", "1234");
			System.out.println("Successfully Connected \n");

			System.out.println("Please wait creating session...");
			Session s = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
			System.out.println("Session created \n");

			System.out.println("Please wait Creating Queue...");
			// create queue
			Destination d;
			d = s.createQueue("subject");
			System.out.println("Queue created \n");

			System.out.println("Please wait Creating Producer...");
			// create producer/sender
			MessageProducer mp;
			mp = s.createProducer(d);
			System.out.println("Producer created \n");

			System.out.println("Please wait Connection Starting...");
			con.start();
			System.out.println("Connection Started \n");

			System.out.println("Please wait Creating TextMessage..");
			// We will send a small text message saying 'Hello' in Japanese
			TextMessage message = s.createTextMessage("Hi How are you!");
			System.out.println("TextMessage Created \n");

			System.out.println("Please wait TextMessage Sending...");
			// Here we are sending the message!
			mp.send(message);
			System.out.println("TextMessage Sent '" + message.getText() + "'");
			System.out.println("Success");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}
}
