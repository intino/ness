package io.intino.ness;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class StockFaucet implements Faucet {

	private Iterator<Stock.Bundle> bundles;
	private MessageInputStream messageInputStream;

	public StockFaucet(Stock stock) {
		this.bundles = Arrays.asList(stock.bundles()).iterator();
		this.messageInputStream = nextInputStream();
	}

	public Message next() throws IOException {
		while (messageInputStream != null) {
			Message message = messageInputStream.next();
			if (message != null) return message;
			messageInputStream.close();
			messageInputStream = nextInputStream();
		}
		return null;
	}

	private MessageInputStream nextInputStream() {
		try {
			if (!bundles.hasNext()) {
				bundles = null;
				return null;
			}
			return bundles.next().input();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
