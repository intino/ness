import io.intino.alexandria.Timetag;
import io.intino.alexandria.zet.Zet;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static io.intino.alexandria.Scale.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Timetag_ {

	private LocalDateTime dateTime;

	@Before
	public void setUp() {
		dateTime = LocalDateTime.of(2018, 2, 28, 10, 20);
	}

	@Test
	public void given_a_tag_should_calculate_instant() {
		assertThat(Timetag.of("201802281020").datetime()).isEqualTo(dateTime);
		assertThat(Timetag.of("201802281020").label()).isEqualTo("2018-02-28-10-20");
	}

	@Test
	public void given_a_tag_should_calculate_scale() {
		assertThat(Timetag.of("2018").scale()).isEqualTo(Year);
		assertThat(Timetag.of("201802").scale()).isEqualTo(Month);
		assertThat(Timetag.of("20180228").scale()).isEqualTo(Day);
		assertThat(Timetag.of("2018022810").scale()).isEqualTo(Hour);
		assertThat(Timetag.of("201802281020").scale()).isEqualTo(Minute);
	}

	@Test
	public void given_an_instant_scale_should_calculate_timetag() {
		assertThat(Timetag.of(dateTime, Year)).isEqualTo(Timetag.of("2018"));
		assertThat(Timetag.of(dateTime, Month)).isEqualTo(Timetag.of("201802"));
		assertThat(Timetag.of(dateTime, Day)).isEqualTo(Timetag.of("20180228"));
		assertThat(Timetag.of(dateTime, Hour)).isEqualTo(Timetag.of("2018022810"));
		assertThat(Timetag.of(dateTime, Minute)).isEqualTo(Timetag.of("201802281020"));
	}

	@Test
	public void given_a_tag_should_calculate_next() {
		assertThat(Timetag.of("2018").next()).isEqualTo(Timetag.of("2019"));
		assertThat(Timetag.of("201801").next()).isEqualTo(Timetag.of("201802"));
		assertThat(Timetag.of("20180228").next()).isEqualTo(Timetag.of("20180301"));
	}

	@Test
	public void given_a_tag_should_calculate_previous() {
		assertThat(Timetag.of("2018").previous()).isEqualTo(Timetag.of("2017"));
		assertThat(Timetag.of("201801").previous()).isEqualTo(Timetag.of("201712"));
		assertThat(Timetag.of("20180228").previous()).isEqualTo(Timetag.of("20180227"));
	}

}
