package io.intino.ness.setstore.session;

import io.intino.ness.setstore.file.FileSetStore;
import io.intino.sezzet.operators.FileReader;
import io.intino.sezzet.operators.SetStream;
import io.intino.sezzet.operators.Union;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.intino.ness.setstore.file.FileSetStore.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;

public class SessionSealer {
	private final List<File> files;
	private File storeFolder;

	private SessionSealer(List<File> files, File storeFolder) {
		this.files = files;
		this.storeFolder = storeFolder;
	}

	public static void seal(File stageFolder) {
		File[] sessions = stageFolder.listFiles(f -> f.getName().endsWith(SessionExt));
		if (sessions == null) return;
		Map<String, List<File>> sessionsMap = groupSessions(sessions);
		sessionsMap.values().forEach(v -> new SessionSealer(v, stageFolder.getParentFile()).seal());
		markAsProcessed(stageFolder, sessionsMap);
	}

	private static void markAsProcessed(File stageFolder, Map<String, List<File>> filesMap) {
		String instant = Instant.now().toString().substring(0, 19).replace(":", "").replace("-", "");
		File processedFolder = new File(stageFolder, "processed/" + instant);
		processedFolder.mkdirs();
		filesMap.values().forEach(v -> v.forEach(f -> {
			try {
				Files.move(f.toPath(), new File(processedFolder, f.getName()).toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}

	private static Map<String, List<File>> groupSessions(File[] sessions) {
		Map<String, List<File>> filesMap = new HashMap<>();
		for (File session : sessions) {
			String sealId = session.getName().substring(0, session.getName().indexOf(PartExt));
			if (!filesMap.containsKey(sealId)) filesMap.put(sealId, new ArrayList<>());
			filesMap.get(sealId).add(session);
		}
		return filesMap;
	}

	private void seal() {
		List<SessionFileReader> readers = loadReaders();
		Set<String> distinctChunks = distinctChunks(readers);
		Logger.getGlobal().info("Sets to seal " + distinctChunks.size());
		distinctChunks.parallelStream().forEach(distinctChunk -> {
			try {
				List<SetStream> streams = chunksOf(readers, distinctChunk);
				File setFile = filepath(distinctChunk);
				File tempFile = new File(filepath(distinctChunk) + TempExt);
				if (setFile.exists()) streams.add(new FileReader(setFile));
				FileSetStore.write(new Union(streams), tempFile);
				Files.move(tempFile.toPath(), setFile.toPath(), REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private List<SessionFileReader> loadReaders() {
		return files.parallelStream().map(f -> {
			try {
				return new SessionFileReader(f);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(toList());
	}

	private File filepath(String distinctChunk) {
		File output = new File(storeFolder, type(distinctChunk) + "/" + timeTag() + "/" + argument(distinctChunk) + SetExt);
		output.getParentFile().mkdirs();
		return output;
	}

	private String timeTag() {
		return files.get(0).getName().substring(0, files.get(0).getName().indexOf(PartExt));
	}

	private List<SetStream> chunksOf(List<SessionFileReader> readers, String distinctChunk) {
		return readers.stream()
				.map(r -> r.chunks(type(distinctChunk), argument(distinctChunk)))
				.flatMap(Collection::stream)
				.map(SessionFileReader.Chunk::stream)
				.collect(toList());
	}

	private String argument(String distinctChunk) {
		return distinctChunk.split("@@")[1];
	}

	private String type(String distinctChunk) {
		return distinctChunk.split("@@")[0];
	}

	private Set<String> distinctChunks(List<SessionFileReader> readers) {
		return readers.stream()
				.map(SessionFileReader::chunks)
				.flatMap(Collection::stream)
				.map(c -> c.tank() + "@@" + c.set())
				.collect(Collectors.toSet());
	}

}
