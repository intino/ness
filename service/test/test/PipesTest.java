package test;

import org.junit.Test;

import javax.jms.JMSException;

public class PipesTest {

	@Test
	public void should_work_pipes() throws JMSException {
		Consumer.main(new String[0]);
		Feeder.main(new String[0]);
	}
}
