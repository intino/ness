package io.intino.ness.triton.datalake.adapter;

import java.io.File;
import java.io.InputStream;

public class Context {
	private final File stageFolder;
	private final File workspaceFolder;
	private String configuration;
	private InputStream attachment;

	public Context(File stageFolder, File workspaceFolder) {
		this.stageFolder = stageFolder;
		this.workspaceFolder = workspaceFolder;
	}

	public Context(File stageFolder, File workspaceFolder, String configuration, InputStream attachment) {
		this.stageFolder = stageFolder;
		this.workspaceFolder = workspaceFolder;
		this.configuration = configuration;
		this.attachment = attachment;
	}

	public File stageFolder() {
		return stageFolder;
	}

	public File workspaceFolder() {
		return workspaceFolder;
	}
}
