package io.intino.ness.core;

import java.io.OutputStream;

public interface BlobHandler {
	OutputStream start(Blob.Type type);

	OutputStream start(String prefix, Blob.Type type);
}
