package io.intino.ness.konos;

import org.junit.Test;

import java.io.File;

public class WrapperTest {

	@Test
	public void startWrapper() throws Exception {
		new Ness(new File("./temp/"), "xoxb-162074419812-gB5oNUwzxGWQ756TrRyu1Ii9").start();
		Thread.currentThread().sleep(10000);
	}
}
