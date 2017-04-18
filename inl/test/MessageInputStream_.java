import io.intino.ness.inl.Formats;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import static messages.Messages.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class MessageInputStream_ {

    @Test
    public void should_parse_multiple_messages() throws Exception {
        InputStream is = inputStreamOf(status());
        MessageInputStream messageInputStream = Formats.Inl.of(is);
        Message[] messages = new Message[3];
        messages[0] = messageInputStream.next();
        messages[1] = messageInputStream.next();
        messages[2] = messageInputStream.next();
        assertThat(messages[0].is("status"), is(true));
        assertThat(messages[0].contains("battery"), is(true));
        assertThat(messages[0].contains("isPlugged"), is(true));
        assertThat(messages[0].contains("created"), is(true));
        assertThat(messages[0].contains("xxxx"), is(false));
        assertThat(messages[0].parse("battery").as(Double.class), is(78.0));
        assertThat(messages[0].parse("isPlugged").as(Boolean.class), is(true));
        assertThat(messages[0].parse("created").as(Date.class).toString(), is("Wed Mar 22 12:56:18 GMT 2017"));
        assertThat(messages[2], is(nullValue()));
    }

    @Test
    public void should_ignore_empty_attributes() throws Exception {
        InputStream is = inputStreamOf(messageWithEmptyAttributes());
        Message message = Formats.Inl.of(is).next();
        assertThat(message.is("teacher"), is(true));
        assertThat(message.contains("name"), is(true));
        assertThat(message.contains("money"), is(true));
        assertThat(message.contains("BirthDate"), is(true));
        assertThat(message.components("country").get(0).contains("name"), is(true));
        assertThat(message.components("country").get(0).contains("continent"), is(false));
        assertThat(message.components("country").get(0).parse("name").as(String.class), is("Spain"));
        assertThat(message.components("country").get(0).parse("continent").as(String.class), is(nullValue()));

    }

    @Test
    public void should_parse_multiline_attributes() throws Exception {
        InputStream is = inputStreamOf(crash());
        Message message = Formats.Inl.of(is).next();
        assertThat(message.type(), is("crash"));
        assertThat(message.contains("instant"), is(true));
        assertThat(message.contains("app"), is(true));
        assertThat(message.contains("deviceId"), is(true));
        assertThat(message.contains("stack"), is(true));
        assertThat(message.parse("instant").as(Date.class).toString(), is("Tue Mar 21 07:39:00 GMT 2017"));
        assertThat(message.parse("app").as(String.class), is("io.intino.consul"));
        assertThat(message.parse("deviceId").as(String.class), is("b367172b0c6fe726"));
        assertThat(message.parse("stack").as(String.class), is(stack().trim()));
    }

    @Test
    public void should_parse_message_with_multiple_components() throws Exception {
        InputStream is = inputStreamOf(messageWithMultipleComponents());
        Message message = Formats.Inl.of(is).next();
        assertThat(message.type(), is("Teacher"));
        assertThat(message.components("country").size(), is(1));
        assertThat(message.components("country").get(0).type(), is("Country"));
        assertThat(message.components("country").get(0).parse("name").as(String.class), is("Spain"));
        assertThat(message.components("phone").size(), is(2));
        assertThat(message.toString(), is(messageWithMultipleComponents().trim()));
    }

    @Test
    public void should_parse_message_in_old_format() throws Exception {
        InputStream is = inputStreamOf(messageInOldFormat());
        Message message = Formats.Inl.of(is).next();
        assertThat(message.type(), is("ActiveTeacher"));
        assertThat(message.parse("name").as(String.class), is("Jose"));
        assertThat(message.components("country").size(), is(1));
        assertThat(message.components("country").get(0).type(), is("Country"));
        assertThat(message.components("country").get(0).parse("name").as(String.class), is("Spain"));

        message.type("Teacher");
        assertThat(message.toString(), is(messageWithParentClass().trim()));
    }

    @Test
    public void should_parse_messages_in_csv() throws Exception {
        InputStream is = inputStreamOf(messagesInCsv());
        MessageInputStream mis = Formats.Csv.of(is);
        Message message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.parse("date").as(String.class), is("16/12/2006"));
        assertThat(message.parse("time").as(String.class), is("17:24:00"));
        assertThat(message.parse("sub_metering_3").as(Double.class), is(17.0));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.parse("date").as(String.class), is("16/12/2006"));
        assertThat(message.parse("time").as(String.class), is("17:25:00"));
        assertThat(message.parse("sub_metering_3").as(Double.class), is(16.0));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.parse("date").as(String.class), is("16/12/2006"));
        assertThat(message.parse("time").as(String.class), is("17:26:00"));
        assertThat(message.parse("sub_metering_3").as(Double.class), is(17.0));

        message = mis.next();
        assertThat(message, is(nullValue()));

    }

    @Test
    public void should_parse_messages_in_data() throws Exception {
        InputStream is = inputStreamOf(messagesInDat());
        MessageInputStream mis = Formats.Dat.of(is);
        Message message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.parse("date").as(String.class), is("16/12/2006"));
        assertThat(message.parse("time").as(String.class), is("17:24:00"));
        assertThat(message.parse("sub_metering_3").as(Double.class), is(17.0));
        assertThat(message.parse("class").as(String.class), is("01"));
        assertThat(message.parse("building").as(String.class), is("01"));
        assertThat(message.parse("room").as(String.class), is("HZG"));

        message = mis.next();
        assertThat(message.type(), is(""));
        assertThat(message.parse("date").as(String.class), is("16/12/2006"));
        assertThat(message.parse("time").as(String.class), is("17:25:00"));
        assertThat(message.parse("sub_metering_3").as(Double.class), is(16.0));
        assertThat(message.parse("class").as(String.class), is("01"));
        assertThat(message.parse("building").as(String.class), is("01"));
        assertThat(message.parse("room").as(String.class), is("HZG"));

        message = mis.next();
        assertThat(message.type(), is("PowerConsumption"));
        assertThat(message.parse("date").as(String.class), is("16/12/2006"));
        assertThat(message.parse("time").as(String.class), is("17:26:00"));
        assertThat(message.parse("sub_metering_3").as(Double.class), is(17.0));
        assertThat(message.parse("class").as(String.class), is("01"));
        assertThat(message.parse("building").as(String.class), is("01"));
        assertThat(message.parse("room").as(String.class), is("HZG"));

        message = mis.next();
        assertThat(message, is(nullValue()));

    }

    private InputStream inputStreamOf(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }


}
