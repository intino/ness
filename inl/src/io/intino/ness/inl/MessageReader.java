package io.intino.ness.inl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageReader {

    public static List<Message> readAll(MessageInputStream input) throws IOException {
        List<Message> messages = new ArrayList<>();
        while (true) {
            Message message = input.next();
            if (message == null) break;
            messages.add(message);
        }
        input.close();
        return messages;
    }


}
