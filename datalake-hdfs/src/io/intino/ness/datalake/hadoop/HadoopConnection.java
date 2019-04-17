package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.logger.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HadoopConnection {


	private final String uri;
	private final String username;
	private final String password;
	private FileSystem fs;

	public HadoopConnection(String uri, String username, String password) {
		this.uri = uri;
		this.username = username;
		this.password = password;
	}

	public void connect() {
		try {
			this.fs = FileSystem.get(new URI(uri), conf(uri, username, password));
		} catch (IOException | URISyntaxException e) {
			Logger.error(e);
		}
	}

	public FileSystem fs() {
		return fs;
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
}
