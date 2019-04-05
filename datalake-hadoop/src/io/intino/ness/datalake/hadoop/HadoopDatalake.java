package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.datalake.Datalake;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static io.intino.ness.datalake.hadoop.Paths.*;

public class HadoopDatalake implements Datalake {
	private final FileSystem fs;
	private final Path root;
	private final HadoopStage stage;

	public HadoopDatalake(String uri, String username, String password) throws IOException, URISyntaxException {
		this.fs = FileSystem.get(new URI(uri), conf(uri, username, password));
		this.root = new Path(fs.getWorkingDirectory(), "datalake");
		this.stage = new HadoopStage(fs, stagePath(root), sessionsPath(root));
		mkdirs();
	}

	@Override
	public EventStore eventStore() {
		return new HadoopEventStore(fs, eventStorePath(root));
	}

	@Override
	public SetStore setStore() {
		return new HadoopSetStore(fs, setStorePath(root));
	}

	private Configuration conf(String uri, String username, String password) {
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", uri);
		return conf;
	}

	private LoginContext kinit(String user, String password) throws LoginException {
		LoginContext lc = new LoginContext(this.getClass().getSimpleName(), callbacks -> {
			for (Callback c : callbacks) {
				if (c instanceof NameCallback)
					((NameCallback) c).setName(user);
				if (c instanceof PasswordCallback)
					((PasswordCallback) c).setPassword(password.toCharArray());
			}
		});
		lc.login();
		return lc;
	}

	private void mkdirs() {
		try {
			if (!fs.exists(root)) fs.mkdirs(root);
			if (!fs.exists(stagePath(root))) fs.mkdirs(stagePath(root));
			if (!fs.exists(setStorePath(root))) fs.mkdirs(setStorePath(root));
			if (!fs.exists(eventStorePath(root))) fs.mkdirs(eventStorePath(root));
			if (!fs.exists(sessionsPath(root))) fs.mkdirs(sessionsPath(root));
			if (!fs.exists(tempPath(root))) fs.mkdirs(tempPath(root));
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
