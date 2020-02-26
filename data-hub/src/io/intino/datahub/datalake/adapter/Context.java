package io.intino.datahub.datalake.adapter;


import java.io.File;

public class Context {
	private final File workspaceFolder;

	public Context(File workspaceFolder) {
		this.workspaceFolder = workspaceFolder;
	}

	public File workspaceFolder() {
		return workspaceFolder;
	}

}
