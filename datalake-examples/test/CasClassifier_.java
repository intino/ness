import io.tgs.toolbox.CasClassifier;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class CasClassifier_ {

    @Test
    public void name() throws Exception {
        assertThat(CasClassifier.of("9025-56-3"), is("!9025-56-3"));
    }
}
