import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageInputStreamFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static io.intino.ness.inl.MessageInputStreamFormat.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static messages.Messages.messageWithMultipleComponents;
import static messages.Messages.status;

public class Message_ {

    Message message;

    @Before
    public void setUp() throws Exception {
        InputStream is = inputStreamOf(status());
        message = Inl.of("test", is).next();
    }

    @Test
    public void should_modify_and_create_new_attributes() throws Exception {
        message.write("battery", 80.0);
        message.write("isPlugged", true);
        message.write("taps", 100);
        assertThat(message.parse("battery").as(Double.class), is(80.0));
        assertThat(message.toString(), is("[Status]\nbattery: 80.0\ncpuUsage: 11.95\nisPlugged: true\nisScreenOn: false\ntemperature: 29.0\ncreated: 2017-03-22T12:56:18Z\ntaps: 100"));
    }

    @Test
    public void should_remove_attributes() throws Exception {
        message.remove("battery");
        message.remove("isscreenon");
        assertThat(message.contains("battery"), is(false));
        assertThat(message.contains("isScreenOn"), is(false));
        assertThat(message.contains("isPlugged"), is(true));
    }

    @Test
    public void should_rename_attributes() throws Exception {
        message.rename("isPlugged", "plugged");
        message.rename("battery", "b");
        assertThat(message.contains("battery"), is(false));
        assertThat(message.contains("b"), is(true));
        assertThat(message.contains("isPlugged"), is(false));
        assertThat(message.contains("plugged"), is(true));
    }

    @Test
    public void should_change_type() throws Exception {
        message.type("sensor");
        assertThat(message.is("sensor"), is(true));
        assertThat(message.contains("battery"), is(true));
    }

    @Test
    public void should_add_and_remove_components() throws Exception {
        InputStream is = inputStreamOf(messageWithMultipleComponents());
        Message message = Inl.of("test", is).next();

        message.remove(message.components("phone").get(0));
        assertThat(message.components("phone").size(), is(1));
        assertThat(message.components("country").size(), is(1));

        message.remove(message.components("phone"));
        assertThat(message.components("phone").size(), is(0));
        assertThat(message.components("country").size(), is(1));

        message.add(new Message("phone"));
        message.components("phone").get(0).write("value","+345101023");
        message.components("phone").get(0).add(new Message("Country"));
        message.components("phone").get(0).components("country").get(0).write("name","Spain");
        message.type("Professor");
        assertThat(message.toString(), is("[Professor]\nname: Jose\nmoney: 50.0\nbirthDate: 2016-10-04T20:10:11Z\nuniversity: ULPGC\n\n[Professor.Country]\nname: Spain\n\n[Professor.phone]\nvalue: +345101023\n\n[Professor.phone.Country]\nname: Spain"));
    }

    @Test
    public void should_serialize_and_deserialize_multiline_attributes() throws Exception {
        Message message = new Message("Multiline");
        message.write("comment", "hello\nworld\n!!!");
        byte[] bytes = message.toString().getBytes();
        MessageInputStream stream = Inl.of("test", new ByteArrayInputStream(bytes));
        Message parsed = stream.next();
        assertThat(parsed.read("comment"), is("hello\nworld\n!!!"));


    }

    private InputStream inputStreamOf(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }
}
