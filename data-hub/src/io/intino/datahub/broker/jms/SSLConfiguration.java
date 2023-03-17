package io.intino.datahub.broker.jms;

import java.io.File;

public record SSLConfiguration(File keyStore, File trustStore, char[] keyStorePassword, char[] trustStorePassword) {
}
