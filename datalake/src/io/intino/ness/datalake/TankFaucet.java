package io.intino.ness.datalake;

import io.intino.ness.datalake.Tank.Tub;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.io.IOException;
import java.util.Iterator;

import static java.util.Arrays.stream;

public class TankFaucet implements Faucet {

    private final Tank tank;
    private final Iterator<Tub> tubs;
    private MessageInputStream inputStream;

    public TankFaucet(Tank tank) {
        this.tank = tank;
        this.tubs = stream(tank.tubs()).iterator();
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
            inputStream = nextInputStream();
        }
        return null;
    }

    private MessageInputStream nextInputStream() {
        try {
            if (!tubs.hasNext()) return null;
            return tubs.next().input();
        }
        catch (Exception e) {
            e.printStackTrace();
            return new MessageInputStream.Empty();
        }
    }
}
