package io.intino.ness.datalake.toolbox;

import io.intino.ness.datalake.*;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Import implements Feeder {

    private File folder;
    private List<MessageMapper> mappers = new ArrayList<>();
    private Joint joint = null;

    private Import(File folder) {
        this.folder = folder;
    }

    public static Import from(String filename) {
        return from(new File(filename));
    }

    public static Import from(File file) {
        if (!file.exists()) throw new RuntimeException("'" + file.getAbsolutePath() + "' doesn't exist");
        return new Import(file);
    }


    public Import join(Joint joint) {
        this.joint = joint;
        return this;
    }

    @Override
    public Job to(Feed feed) {
        Faucet faucet = new TankFaucet(new FileTank(folder, joint));
        return new Job() {

            @Override
            protected boolean step() {
                try {
                    Message message  = cast(faucet.next());
                    if (message == null) return false;
                    feed.send(message);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onTerminate() {
                feed.flush();
            }
        };
    }

    private Message cast(Message message) {
        for (MessageMapper mapper : mappers) {
            if (message == null) return null;
            message = mapper.map(message);
        }
        return message;
    }

    public Import map(MessageMapper mapper) {
        mappers.add(mapper);
        return this;
    }


    public interface Joint {
        MessageInputStream join(MessageInputStream[] inputStreams);
    }


    public static Joint sortingBy(String attribute) {
        return inputStreams -> {
            try {
                return new Sorter(inputStreams, attribute);
            } catch (IOException e) {
                e.printStackTrace();
                return new MessageInputStream.Empty();
            }
        };
    }


    private static class Sorter implements MessageInputStream {
        private MessageInputStream[] inputStreams;
        private Message[] messages;
        private String attribute;

        Sorter(MessageInputStream[] inputStreams, String attribute) throws IOException {
            this.inputStreams = inputStreams;
            this.messages = new Message[inputStreams.length];
            this.attribute = attribute;
            for (int i = 0; i < messages.length; i++)
                this.messages[i] = inputStreams[i].next();
        }

        @Override
        public String name() {
            return "sort join";
        }

        @Override
        public void name(String value) {

        }

        @Override
        public Message next() throws IOException {
            int index = indexOfNext();
            if (index == -1) return null;
            Message message = messages[index];
            messages[index] = inputStreams[index].next();
            return message;
        }

        @Override
        public void close() throws IOException {
            for (MessageInputStream inputStream : inputStreams)
                inputStream.close();
        }

        private int indexOfNext() {
            int index = -1;
            String min = "z";
            String val;
            for (int i = 0; i < messages.length; i++) {
                if (messages[i] == null) continue;
                val = messages[i].read(attribute);
                if (val.compareTo(min) >= 0) continue;
                index = i;
                min = val;
            }
            return index;
        }

    }

}
