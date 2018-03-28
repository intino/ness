package test;

import io.intino.ness.datalake.MessageExternalSorter;
import org.junit.Test;

import java.io.File;

public class Sorter {


	@Test
	public void sort() throws InterruptedException {
		Thread.sleep(5000);

		final long x = System.currentTimeMillis();
		new MessageExternalSorter(new File("/Users/oroncal/workspace/ness/application/temp/2018021821.inl")).sort();
		System.out.print(((System.currentTimeMillis() - x) / 1000) + " sg");
	}
}
