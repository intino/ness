package io.intino.test;

import org.junit.Ignore;
import org.junit.Test;

import javax.jms.JMSException;

public class PipesTest {

	@Test
	@Ignore
	public void should_work_pipes() throws JMSException {
		Consumer.main(new String[0]);
		Feeder.main(new String[0]);
	}
}
