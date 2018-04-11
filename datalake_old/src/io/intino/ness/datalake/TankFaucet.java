package io.intino.ness.datalake;

import io.intino.ness.datalake.Tank.Tub;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class TankFaucet implements Faucet {

	private final Tank tank;
	private Iterator<Tub> tubs;
	private MessageInputStream inputStream;

	public TankFaucet(Tank tank) {
		this.tank = tank;
		this.tubs = Arrays.asList(tank.tubs()).iterator();
		this.inputStream = nextInputStream();
	}

	public Tank tank() {
		return tank;
	}

	public String name() {
		return tank.name();
	}

	public Message next() throws IOException {
		while (inputStream != null) {
			Message message = inputStream.next();
			if (message != null) return message;
			this.inputStream.close();
			this.inputStream = nextInputStream();
		}
		return null;
	}

	private MessageInputStream nextInputStream() {
		try {
			if (!tubs.hasNext()) {
				this.tubs = null;
				return null;
			}
			return this.tubs.next().input();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
