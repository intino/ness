import io.intino.ness.inl.Message;
import org.junit.Before;
import org.junit.Test;

import static messages.Messages.MenuWithOnePriceMessage;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static messages.Messages.MultipleComponentMessage;

public class Message_ {

    private Message message;

    @Before
    public void setUp() throws Exception {
        String statusMessage =
                "[Status]\n" +
                "battery: 78.0\n" +
                "cpuUsage: 11.95\n" +
                "isPlugged: true\n" +
                "isScreenOn: false\n" +
                "temperature: 29.0\n" +
                "created: 2017-03-22T12:56:18Z\n";
        message = Message.load(statusMessage);
    }

    @Test
    public void should_override_and_create_new_attributes() throws Exception {
        message.set("battery", 80.0);
        message.set("taps", 100);
        assertThat(message.read("battery").as(Double.class), is(80.0));
        assertThat(message.read("taps").as(Integer.class), is(100));
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
        message
				.rename("isPlugged", "plugged")
				.rename("battery", "b");
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
    public void should_handle_components() throws Exception {
        Message message = Message.load(MultipleComponentMessage);

        message.remove(message.components("phone").get(0));
        assertThat(message.components("phone").size(), is(1));
        assertThat(message.components("country").size(), is(1));

        message.remove(message.components("phone"));
        assertThat(message.components("phone").size(), is(0));
        assertThat(message.components("country").size(), is(1));

        message.add(new Message("phone"));
        message.components("phone").get(0).set("value","+345101023");
        message.components("phone").get(0).add(new Message("Country"));
        message.components("phone").get(0).components("country").get(0).set("name","Spain");
        message.type("Professor");
        assertThat(message.toString(), is("[Professor]\nname: Jose\nmoney: 50.0\nbirthDate: 2016-10-04T20:10:11Z\nuniversity: ULPGC\n\n[Professor.Country]\nname: Spain\n\n[Professor.phone]\nvalue: +345101023\n\n[Professor.phone.Country]\nname: Spain"));
    }

    @Test
    public void should_handle_multi_line_attributes() throws Exception {
        Message message = new Message("Multiline");
		message.write("name", "John");
		message.write("age", 30);
		message.write("age", 20);
		message.write("comment", "hello");
		message.write("comment", "world");
		message.write("comment", "!!!");
		String text = message.toString();
		Message parsed = Message.load(text);
        assertThat(parsed.get("age"), is("30\n20"));
        assertThat(parsed.get("comment"), is("hello\nworld\n!!!"));
    }

    @Test
    public void should_handle_array_attributes() throws Exception {
        Message message = Message.load(MenuWithOnePriceMessage);
        assertThat(message.read("meals").as(String[].class).length, is(4));
        assertThat(message.read("meals").as(String[].class)[0], is("Soup"));
        assertThat(message.read("meals").as(String[].class)[1], is("Lobster"));
        assertThat(message.read("prices").as(Double[].class).length, is(1));
        assertThat(message.read("prices").as(Double[].class)[0], is(7.0));
        assertThat(message.read("availability").as(Boolean[].class).length, is(2));
        assertThat(message.read("availability").as(Boolean[].class)[0], is(true));
        assertThat(message.read("availability").as(Boolean[].class)[1], is(false));
    }

    @Test
    public void should_handle_document_attributes() throws Exception {
        Message message = new Message("Document");
        message.set("name","my file");
        message.set("file","png", document(64));
        message.set("file","png", document(128));
        assertThat(message.attachments().size(), is(1));
        assertThat(message.attachment(message.get("file")).type(), is("png"));
        assertThat(message.attachment(message.get("file")).data().length, is(128));
	}

    @Test
    public void should_handle_document_list_attributes() throws Exception {
        Message message = new Message("Document");
        message.set("name","my file");
        message.write("file","png", document(20));
        message.write("file","png", document(30));
        message.set("file","png", document(40));
        message.write("file","png", document(80));
        assertThat(message.attachments().size(), is(2));
        assertThat(message.attachments().size(), is(2));
        assertThat(message.attachment(message.get("file").split("\n")[0]).type(), is("png"));
        assertThat(message.attachment(message.get("file").split("\n")[0]).data().length, is(40));
	}

	@Test
	public void should_load_documents() throws Exception {
		String text = "[Document]\n" +
				"name: my file\n" +
				"file: @7f7a9352-8b54-465b-8e63-15c7586f01e9.png";
		Message message = Message.load(text);
		for (Message.Attachment attachment : message.attachments())
			attachment.data(document(128));
		assertThat(message.attachments().size(), is(1));
		assertThat(message.attachment(message.get("file")).type(), is("png"));
		assertThat(message.attachment(message.get("file")).data().length, is(128));
	}

	private byte[] document(int size) {
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++) data[i] = (byte) i;
		return data;
	}

}
