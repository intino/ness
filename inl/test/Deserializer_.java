import schemas.Crash;
import schemas.CrashX;
import schemas.Menu;
import schemas.Teacher;
import org.junit.Test;

import java.util.Date;

import static io.intino.ness.inl.Deserializer.deserialize;
import static messages.Messages.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class Deserializer_ {

    @Test
    public void should_deserialize_messages_in_a_class_with_parent() throws Exception {
        Teacher teacher = deserialize(messageWithParentClass()).next(Teacher.class);
        assertThat(teacher.name, is("Jose"));
        assertThat(teacher.money, is(50.0));
        assertThat(teacher.birthDate, is(date(2016, 10, 4, 20, 10, 11)));
        assertThat(teacher.university, is("ULPGC"));
        assertThat(teacher.country.name, is("Spain"));
        assertThat(teacher.country.continent, is(nullValue()));
    }

    @Test
    public void should_deserialize_message_with_empty_attributes() throws Exception {
        Teacher teacher = deserialize(messageWithEmptyAttributes()).next(Teacher.class);
        assertThat(teacher.name, is("Jose"));
        assertThat(teacher.money, is(50.0));
        assertThat(teacher.birthDate, is(date(2016, 10, 4, 20, 10, 11)));
        assertThat(teacher.university, is("ULPGC"));
        assertThat(teacher.country.name, is("Spain"));
        assertThat(teacher.country.continent, is(nullValue()));
    }

    @Test
    public void should_deserialize_messages_with_array_attributes() throws Exception {
        Menu menu = deserialize(menu()).next(Menu.class);
        assertThat(menu.meals.length, is(4));
        assertThat(menu.prices.length, is(4));
        assertThat(menu.availability.length, is(2));
        assertThat(menu.meals[0], is("Soup"));
        assertThat(menu.meals[1], is("Lobster"));
        assertThat(menu.prices[0], is(5.0));
        assertThat(menu.prices[1], is(24.5));
        assertThat(menu.prices[2], is(8.0));
        assertThat(menu.availability[0], is(true));
        assertThat(menu.availability[1], is(false));
    }

    @Test
    public void should_return_null_if_header_doesnt_match_the_class() throws Exception {
        Crash crash = deserialize(messageWithParentClass()).next(Crash.class);
        assertThat(crash, is(nullValue()));
    }

    @Test
    public void should_deserialize_message_with_multiline_attributes() throws Exception {
        Crash crash = deserialize(crash()).next(Crash.class);
        assertThat(crash.instant.toString(), is("Tue Mar 21 07:39:00 GMT 2017"));
        assertThat(crash.app, is("io.intino.consul"));
        assertThat(crash.deviceId, is("b367172b0c6fe726"));
        assertThat(crash.stack, is(stack().trim()));
    }

    @Test
    public void should_map_identifiers() throws Exception {
        CrashX crash = deserialize(crash()).
                map("Crash", "CrashX").
                map("instant", "ts").
                map("app", "application").
                map("deviceId", "device").
                map("stack", "lines").
                next(CrashX.class);

        assertThat(crash.ts.toString(), is("Tue Mar 21 07:39:00 GMT 2017"));
        assertThat(crash.application, is("io.intino.consul"));
        assertThat(crash.device, is("b367172b0c6fe726"));
        assertThat(crash.lines.length, is(10));
        assertThat(crash.lines[0], is("java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object java.util.List.get(int)' on a null object reference"));
        assertThat(crash.lines[1], is("    at io.intino.consul.AppService$5.run(AppService.java:154)"));
    }

    @Test
    public void should_deserialize_message_with_multiple_components() throws Exception {
        Teacher teacher = deserialize(messageWithMultipleComponents()).next(Teacher.class);
        assertThat(teacher.name, is("Jose"));
        assertThat(teacher.money, is(50.0));
        assertThat(teacher.birthDate, is(date(2016, 10, 4, 20, 10, 11)));
        assertThat(teacher.country.name, is("Spain"));
        assertThat(teacher.phones.size(), is(2));
        assertThat(teacher.phones.get(0).value, is("+150512101402"));
        assertThat(teacher.phones.get(0).country.name, is("USA"));
        assertThat(teacher.phones.get(1).value, is("+521005101402"));
        assertThat(teacher.phones.get(1).country.name, is("Mexico"));
    }

    @Test
    public void should_deserialize_message_in_old_format() throws Exception {
        Teacher teacher = deserialize(messageInOldFormat()).next(Teacher.class);
        assertThat(teacher.name, is("Jose"));
        assertThat(teacher.money, is(50.0));
        assertThat(teacher.birthDate, is(date(2016, 10, 4, 20, 10, 11)));
        assertThat(teacher.university, is("ULPGC"));
        assertThat(teacher.country.name, is("Spain"));
        assertThat(teacher.country.continent, is(nullValue()));
    }

    private Date date(int y, int m, int d, int h, int mn, int s) {
        return new Date(y-1900,m-1,d,h,mn,s);
    }

}
