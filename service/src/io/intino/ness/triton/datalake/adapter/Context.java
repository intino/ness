package io.intino.ness.triton.datalake.adapter;

import io.intino.ness.datalake.Datalake;

import java.io.File;

public class Context {
	private final Datalake datalake;
	private final File workspaceFolder;

	public Context(Datalake datalake, File workspaceFolder) {
		this.datalake = datalake;
		this.workspaceFolder = workspaceFolder;
	}

	public Datalake datalake() {
		return datalake;
	}

	public File workspaceFolder() {
		return workspaceFolder;
	}

}
