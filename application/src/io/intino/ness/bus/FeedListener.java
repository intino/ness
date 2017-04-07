package io.intino.ness.bus;

import io.intino.ness.Ness;
import io.intino.ness.datalake.FileDataLake;
import io.intino.ness.konos.NessBox;

import javax.jms.Message;
import javax.jms.MessageListener;

public final class FeedListener implements MessageListener {

	private NessBox nessBox;
	private Ness ness;
	private String topic;

	FeedListener(NessBox nessBox, Ness ness, String topic) {
		this.nessBox = nessBox;
		this.ness = ness;
		this.topic = topic;
	}

	@Override
	public void onMessage(Message message) {
		FileDataLake fileDataLake = nessBox.get(FileDataLake.class);
		fileDataLake.manage().create(topic);
	}
}
