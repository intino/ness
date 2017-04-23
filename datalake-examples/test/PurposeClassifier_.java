import io.tgs.toolbox.ProductClassifier;
import io.tgs.toolbox.PurposeClassifier;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class PurposeClassifier_ {

    @Test
    public void name() throws Exception {
        assertThat(PurposeClassifier.of("reduce friction"), is("Friction Reducer"));
        assertThat(PurposeClassifier.of("oxygen scavenger"), is("Oxygen Scavenger"));
        assertThat(PurposeClassifier.of("treat water"), is("Biocide"));
        assertThat(PurposeClassifier.of("solution cleanup"), is("Biocide"));
        assertThat(PurposeClassifier.of("fluid base"), is("Carrier"));
        assertThat(PurposeClassifier.of("linker cross"), is("Crosslinker"));
        assertThat(PurposeClassifier.of("loss fluid additive"), is("Fluid Loss"));
        assertThat(PurposeClassifier.of("fluidbase"), is("Carrier"));
        assertThat(PurposeClassifier.of("iron sequesterant"), is("Iron Control"));
        assertThat(PurposeClassifier.of("agent iron reducing"), is("Iron Control"));
        assertThat(PurposeClassifier.of("casnotassigned"), is("Undefined"));
        assertThat(PurposeClassifier.of("notassigned"), is("Undefined"));
        assertThat(PurposeClassifier.of("cleaner"), is("Biocide"));
        assertThat(PurposeClassifier.of("scale remover"), is("Scale Control"));
        assertThat(PurposeClassifier.of("remover scale"), is("Scale Control"));
        assertThat(PurposeClassifier.of("lubricant"), is("!lubricant"));
        assertThat(PurposeClassifier.of("lubricant"), is("!lubricant"));
        assertThat(PurposeClassifier.of("scale dissolver"), is("Scale Control"));
        assertThat(PurposeClassifier.of("agent sequestering iron"), is("Iron Control"));
        assertThat(PurposeClassifier.of("7 1/2% Hrydrochloric Acid"), is("Acidizing"));
        assertThat(PurposeClassifier.of("Braker"), is("Breaker"));
        assertThat(PurposeClassifier.of("Chemical Frac Tracers (CFT)"), is("Tracer"));
        assertThat(PurposeClassifier.of("15% Unihibited HCI Acid"), is("Acidizing"));
        assertThat(PurposeClassifier.of("Scalechek(R) SCP-2 Scale Inhibitor"), is("Scale Control"));
        assertThat(PurposeClassifier.of("scale preventer"), is("Scale Control"));

    }
}
