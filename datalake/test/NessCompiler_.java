import io.intino.ness.datalake.NessCompiler;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFunction;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class NessCompiler_ {

    @Test
    public void should_compile_and_load_code() throws Exception {
        MessageFunction function = NessCompiler.
                compile(upperCaseFunction()).
                with("-target", "1.8").
                load("tests.UpperCaseFunction").
                as(MessageFunction.class).
                newInstance();
        assertThat(function.cast(new Message("Hello World")).type(), is("HELLO WORLD"));
    }

    @Test
    public void should_recompile_and_load_code() throws Exception {
        {
            NessCompiler.
                    compile(upperCaseFunction()).
                    with("-target", "1.8").
                    load("tests.UpperCaseFunction").
                    as(MessageFunction.class).
                    newInstance();
        }
        MessageFunction function = NessCompiler.
                compile(upperCasePlusFunction()).
                with("-target", "1.8").
                load("tests.UpperCaseFunction").
                as(MessageFunction.class).
                newInstance();
        assertThat(function.cast(new Message("Hello World")).type(), is("HELLO WORLD-11"));
    }

    @Test
    public void should_compile_two_classes_and_load_code() throws Exception {
        MessageFunction function = NessCompiler.
                compile(frontFunction(), upperCaseFunction()).
                with("-target", "1.8").
                load("io.yyy.FrontFunction").
                as(MessageFunction.class).
                newInstance();
        assertThat(function.cast(new Message("Hello World")).type(), is("HELLO WORLD"));
    }

    public static String upperCaseFunction() {
        return  "package tests;\n" +
                "\n" +
                "import io.intino.ness.datalake.MessageFunction;\n" +
                "import io.intino.ness.inl.Message;\n" +
                "\n" +
                "public class UpperCaseFunction implements MessageFunction {\n" +
                "   public Message cast(Message input) {\n" +
                "        input.type(input.type().toUpperCase());\n" +
                "        return input;\n" +
                "   }\n" +
                "}";
    }

    public static String upperCasePlusFunction() {
        return  "package tests;\n" +
                "\n" +
                "import io.intino.ness.datalake.MessageFunction;\n" +
                "import io.intino.ness.inl.Message;\n" +
                "\n" +
                "public class UpperCaseFunction implements MessageFunction {\n" +
                "   public Message cast(Message input) {\n" +
                "        input.type(input.type().toUpperCase() + \"-\" + input.type().length());\n" +
                "        return input;\n" +
                "   }\n" +
                "}";
    }

    public static String frontFunction() {
        return  "package io.yyy;\n" +
                "\n" +
                "import io.intino.ness.datalake.MessageFunction;\n" +
                "import io.intino.ness.inl.Message;\n" +
                "import tests.UpperCaseFunction;\n" +
                "\n" +
                "public class FrontFunction implements MessageFunction {\n" +
                "   public Message cast(Message input) {\n" +
                "        return new UpperCaseFunction().cast(input);\n" +
                "   }\n" +
                "}";
    }
}
