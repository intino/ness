package io.intino.ness.inl.streams;

import io.intino.ness.inl.MessageInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static io.intino.ness.inl.Formats.*;
import static io.intino.ness.inl.MessageInputStream.Empty;
import static io.intino.ness.inl.MessageInputStream.Sort;
import static io.intino.ness.inl.streams.FileMessageInputStream.Format.*;

public class FileMessageInputStream {

	public static MessageInputStream of(File file) throws IOException {
		Format format = formatOf(file);

		if (format == inl) return name(Sort.of(Inl.of(streamOf(file))), file);
		if (format == inz) return name(Inl.of(streamOf(file)), file);
		if (format == csv) return name(Csv.of(streamOf(file)), file);
		if (format == tsv) return name(Tsv.of(streamOf(file)), file);
		if (format == dat) return name(Dat.of(streamOf(file)), file);
		return new Empty();
	}

	private static MessageInputStream name(MessageInputStream inputStream, File file) {
		inputStream.name(file.getName());
		return inputStream;
	}

	public static MessageInputStream of(File... files) throws IOException {
		MessageInputStream[] inputStreams = new MessageInputStream[files.length];
		for (int i = 0; i < files.length; i++) inputStreams[i] = of(files[i]);
		return Sort.of(inputStreams);
	}

	private static InputStream streamOf(File file) throws IOException {
		return isZip(file) ? zipStreamOf(file) : new FileInputStream(file);
	}

	private static boolean isZip(File file) {
		return file.getName().endsWith(".zip");
	}

	private static InputStream zipStreamOf(File file) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file));
		zipStream.getNextEntry();
		return zipStream;
	}

	private static Format formatOf(File file) {
		String name = file.getName().toLowerCase();
		for (String extension : formats.keySet())
			if (name.endsWith(extension)) return formats.get(extension);
		return unknown;
	}

	private static Map<String, Format> formats = new HashMap<>();

	public enum Format {
		inl, inz, csv, tsv, dat, xml, unknown
	}

	static {
		formats.put(".inl", inl);
		formats.put(".zip", inz);
		formats.put(".csv", csv);
		formats.put(".tsv", tsv);
		formats.put(".dat", dat);
		formats.put(".xml", xml);
	}


}
