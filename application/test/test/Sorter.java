package test;

import com.google.code.externalsorting.ExternalSort;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Sorter {


	@Test
	public void sort() throws InterruptedException, IOException {
		Thread.sleep(5000);

		final long x = System.currentTimeMillis();
		final List<File> files = ExternalSort.sortInBatch(new File("/Users/oroncal/workspace/ness/application/test-res/2018021821.inl"));
		ExternalSort.mergeSortedFiles(files, new File("/Users/oroncal/workspace/ness/application/test-res/2018021821.tmp"));
		System.out.print(((System.currentTimeMillis() - x) / 1000) + " sg");
	}
}
