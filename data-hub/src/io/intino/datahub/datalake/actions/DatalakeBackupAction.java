package io.intino.datahub.datalake.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class DatalakeBackupAction {
	public static final String BAK = "bak";
	private static AtomicBoolean started = new AtomicBoolean(false);
	private final DataHubBox box;

	public DatalakeBackupAction(DataHubBox box) {
		this.box = box;
	}

	public boolean isStarted() {
		return started.get();
	}

	public synchronized void execute() {
		if (box.graph().datalake().backup() == null) return;
		if (started.get()) return;
		started.set(true);
		removeOldBacks();
		File backupDirectory = new File(box.graph().datalake().backup().path());
		backupDatalake(backupDirectory);
		backupSessions(new File(backupDirectory, "sessions"));
		Logger.info("Backup finished");
		started.set(false);
	}

	private void removeOldBacks() {
		File root = new File(box.graph().datalake().path());
		List<File> collect = new ArrayList<>(FileUtils.listFiles(root, new String[]{BAK}, true)).stream().
				filter(f -> new Date(f.lastModified()).before(Date.from(Instant.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)))).collect(Collectors.toList());
		for (File file : collect) file.delete();
	}

	private void backupDatalake(File backupDirectory) {
		Logger.info("Launching Backup of datalake...");
		backupDirectory.mkdir();
		File root = new File(box.graph().datalake().path());
		File destination = new File(backupDirectory, name() + ".zip");
		destination.getParentFile().mkdirs();
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destination));
			zipDirectory(root, root.getName(), zos);
			zos.flush();
			zos.close();
		} catch (IOException e) {
			Logger.error(e);
		}
		Logger.info("Datalake Backup finished");
	}

	private String name() {
		return Instant.now().toString().replaceAll("-|:", "").replaceAll("T", "_").substring(0, 13);
	}

	private void backupSessions(File backupDirectory) {
		Logger.info("Launching Backup of sessions...");
		File stage = box.stageDirectory();
		List<File> collect = new ArrayList<>(FileUtils.listFiles(stage, new String[]{"treated"}, true));
		Logger.info(collect.size() + " session to backup");
		for (File session : collect) {
			try {
				if (!session.exists()) continue;
				File destination = new File(backupDirectory, ts(session));
				destination.mkdirs();
				File destinationFile = new File(destination, session.getName());
				if (!destinationFile.exists()) {
					Files.move(session.toPath(), destinationFile.toPath());
				} else session.renameTo(new File(session.getAbsolutePath() + ".duplicated"));
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	private String ts(File session) {
		return new Date(session.lastModified()).toInstant().toString().replace("-", "").substring(0, 8);
	}

	private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
		for (File file : Objects.requireNonNull(folder.listFiles())) {
			if (file.isDirectory()) {
				zipDirectory(file, parentFolder + "/" + file.getName(), zos);
				continue;
			}
			zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] bytesIn = new byte[4096];
			int read;
			while ((read = bis.read(bytesIn)) != -1) zos.write(bytesIn, 0, read);
			zos.closeEntry();
		}
	}
}