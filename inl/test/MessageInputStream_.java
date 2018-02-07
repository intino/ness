import io.intino.ness.inl.Loader;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.TimeZone;

import static messages.Messages.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class MessageInputStream_ {

    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void should_parse_multiple_messages() throws Exception {
        InputStream is = inputStreamOf(StatusMessage);
        MessageInputStream messageInputStream = Loader.Inl.of(is);
        Message[] messages = new Message[3];
        messages[0] = messageInputStream.next();
        messages[1] = messageInputStream.next();
        messages[2] = messageInputStream.next();
        assertThat(messages[0].is("status"), is(true));
        assertThat(messages[0].contains("battery"), is(true));
        assertThat(messages[0].contains("isPlugged"), is(true));
        assertThat(messages[0].contains("created"), is(true));
        assertThat(messages[0].contains("xxxx"), is(false));
        assertThat(messages[0].read("battery").as(Double.class), is(78.0));
        assertThat(messages[0].read("isPlugged").as(Boolean.class), is(true));
        assertThat(messages[0].read("created").as(Date.class).toString(), is("Wed Mar 22 12:56:18 UTC 2017"));
        assertThat(messages[2], is(nullValue()));
    }

    @Test
    public void should_ignore_empty_attributes() throws Exception {
        InputStream is = inputStreamOf(EmptyAttributeMessage);
        Message message = Loader.Inl.of(is).next();
        assertThat(message.is("teacher"), is(true));
        assertThat(message.contains("name"), is(true));
        assertThat(message.contains("money"), is(true));
        assertThat(message.contains("BirthDate"), is(true));
        assertThat(message.components("country").get(0).contains("name"), is(true));
        assertThat(message.components("country").get(0).contains("continent"), is(false));
        assertThat(message.components("country").get(0).read("name").as(String.class), is("Spain"));
        assertThat(message.components("country").get(0).read("continent").as(String.class), is(nullValue()));

    }

    @Test
    public void should_parse_multiline_attributes() throws Exception {
        InputStream is = inputStreamOf(CrashMessage);
        Message message = Loader.Inl.of(is).next();
        assertThat(message.type(), is("Crash"));
        assertThat(message.contains("instant"), is(true));
        assertThat(message.contains("app"), is(true));
        assertThat(message.contains("deviceId"), is(true));
        assertThat(message.contains("stack"), is(true));
        assertThat(message.read("instant").as(Date.class).toString(), is("Tue Mar 21 07:39:00 UTC 2017"));
        assertThat(message.read("app").as(String.class), is("io.intino.consul"));
        assertThat(message.read("deviceId").as(String.class), is("b367172b0c6fe726"));
        assertThat(message.read("stack").as(String.class), is(Stack));
    }

    @Test
    public void should_parse_message_with_multiple_components() throws Exception {
        InputStream is = inputStreamOf(MultipleComponentMessage);
        Message message = Loader.Inl.of(is).next();
        assertThat(message.type(), is("Teacher"));
        assertThat(message.components("country").size(), is(1));
        assertThat(message.components("country").get(0).type(), is("Country"));
        assertThat(message.components("country").get(0).read("name").as(String.class), is("Spain"));
        assertThat(message.components("phone").size(), is(2));
        assertThat(message.toString(), is(MultipleComponentMessage.trim()));
    }

    @Test
    public void should_parse_message_with_files() throws Exception {

    }

    @Test
    public void should_parse_message_in_old_format() throws Exception {
        InputStream is = inputStreamOf(OldFormatMessage);
        Message message = Loader.Inl.of(is).next();
        assertThat(message.type(), is("Teacher"));
        assertThat(message.read("name").as(String.class), is("Jose"));
        assertThat(message.components("country").size(), is(1));
        assertThat(message.components("country").get(0).type(), is("Country"));
        assertThat(message.components("country").get(0).read("name").as(String.class), is("Spain"));

        message.type("Teacher");
        assertThat(message.toString(), is(MessageWithParentClass.trim()));
    }

    @Test
    public void should_parse_messages_in_csv() throws Exception {
        InputStream is = inputStreamOf(CsvMessages);
        MessageInputStream mis = Loader.Csv.of(is);
        Message message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.read("date").as(String.class), is("16/12/2006"));
        assertThat(message.read("time").as(String.class), is("17:24:00"));
        assertThat(message.read("sub_metering_3").as(Double.class), is(17.0));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.read("date").as(String.class), is("16/12/2006"));
        assertThat(message.read("time").as(String.class), is("17:25:00"));
        assertThat(message.read("sub_metering_3").as(Double.class), is(16.0));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.read("date").as(String.class), is("16/12/2006"));
        assertThat(message.read("time").as(String.class), is("17:26:00"));
        assertThat(message.read("sub_metering_3").as(Double.class), is(17.0));

        message = mis.next();
        assertThat(message, is(nullValue()));

    }

    @Test
    public void should_parse_messages_in_data() throws Exception {
        InputStream is = inputStreamOf(DatMessages);
        MessageInputStream mis = Loader.Dat.of(is);
        Message message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.read("date").as(String.class), is("16/12/2006"));
        assertThat(message.read("time").as(String.class), is("17:24:00"));
        assertThat(message.read("sub_metering_3").as(Double.class), is(17.0));
        assertThat(message.read("class").as(String.class), is("01"));
        assertThat(message.read("building").as(String.class), is("01"));
        assertThat(message.read("room").as(String.class), is("HZG"));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.read("date").as(String.class), is("16/12/2006"));
        assertThat(message.read("time").as(String.class), is("17:25:00"));
        assertThat(message.read("sub_metering_3").as(Double.class), is(16.0));
        assertThat(message.read("class").as(String.class), is("01"));
        assertThat(message.read("building").as(String.class), is("01"));
        assertThat(message.read("room").as(String.class), is("HZG"));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.read("date").as(String.class), is("16/12/2006"));
        assertThat(message.read("time").as(String.class), is("17:26:00"));
        assertThat(message.read("sub_metering_3").as(Double.class), is(17.0));
        assertThat(message.read("class").as(String.class), is("01"));
        assertThat(message.read("building").as(String.class), is("01"));
        assertThat(message.read("room").as(String.class), is("HZG"));

        message = mis.next();
        assertThat(message, is(nullValue()));

    }

    private InputStream inputStreamOf(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }


}
