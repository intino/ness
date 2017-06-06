import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class JsonParser {


    private ScriptEngine engine;

    @Before
    public void initEngine() {
        ScriptEngineManager sem = new ScriptEngineManager();
        this.engine = sem.getEngineByName("javascript");
    }

    @Test
    public void parseJson() throws IOException, ScriptException {
        String json = "{\"type\":\"update\",\"eventId\":521238006,\"events\":[{\"type\":\"trade\",\"tid\":521238006,\"price\":\"1235.02\",\"amount\":\"0.09\",\"makerSide\":\"ask\"},{\"type\":\"change\",\"side\":\"ask\",\"price\":\"1235.02\",\"remaining\":\"0.00191269\",\"delta\":\"-0.09\",\"reason\":\"trade\"}]}";
        String script = "JsonParser.fun3(" + json + ")";
        Object result = this.engine.eval(json);
        assertThat(result, instanceOf(Map.class));
        Map contents = (Map) result;
        contents.forEach((t, u) -> {
            System.out.println(t + " " + u);
        });
    }


    static void fun3(ScriptObjectMirror mirror) {
        System.out.println(mirror.getClassName() + ": " +
                Arrays.toString(mirror.getOwnKeys(true)));
    }
}
