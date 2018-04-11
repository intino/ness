import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class Compiler_ {

    @Test
    public void should_compile_and_load_code() throws Exception {
        MessageMapper function = Compiler.
                compile(upperCaseFunction()).
                with("-target", "1.8").
                load("tests.UpperCaseFunction").
                as(MessageMapper.class).
                newInstance();
        assertThat(function.map(new Message("Hello World")).type(), is("HELLO WORLD"));
    }

    @Test
    public void should_recompile_and_load_code() throws Exception {
        {
            Compiler.
                    compile(upperCaseFunction()).
                    with("-target", "1.8").
                    load("tests.UpperCaseFunction").
                    as(MessageMapper.class).
                    newInstance();
        }
        MessageMapper function = Compiler.
                compile(upperCasePlusFunction()).
                with("-target", "1.8").
                load("tests.UpperCaseFunction").
                as(MessageMapper.class).
                newInstance();
        assertThat(function.map(new Message("Hello World")).type(), is("HELLO WORLD-11"));
    }

    @Test
    public void should_compile_two_classes_and_load_code() throws Exception {
        MessageMapper function = Compiler.
                compile(frontFunction(), upperCaseFunction()).
                with("-target", "1.8").
                load("io.yyy.FrontFunction").
                as(MessageMapper.class).
                newInstance();
        assertThat(function.map(new Message("Hello World")).type(), is("HELLO WORLD"));
    }

    public static String upperCaseFunction() {
        return  "package tests;\n" +
                "\n" +
                "import io.intino.ness.inl.MessageMapper;\n" +
                "import io.intino.ness.inl.Message;\n" +
                "\n" +
                "public class UpperCaseFunction implements MessageMapper {\n" +
                "   public Message map(Message input) {\n" +
                "        input.type(input.type().toUpperCase());\n" +
                "        return input;\n" +
                "   }\n" +
                "}";
    }

    public static String upperCasePlusFunction() {
        return  "package tests;\n" +
                "\n" +
                "import io.intino.ness.inl.MessageMapper;\n" +
                "import io.intino.ness.inl.Message;\n" +
                "\n" +
                "public class UpperCaseFunction implements MessageMapper {\n" +
                "   public Message map(Message input) {\n" +
                "        input.type(input.type().toUpperCase() + \"-\" + input.type().length());\n" +
                "        return input;\n" +
                "   }\n" +
                "}";
    }

    public class UpperCaseFunction implements MessageMapper {

        @Override
        public Message map(Message input) {
            input.attributes().add("object=value");
            return input;
        }
    }

    public static String frontFunction() {
        return  "package io.yyy;\n" +
                "\n" +
                "import io.intino.ness.inl.MessageMapper;\n" +
                "import io.intino.ness.inl.Message;\n" +
                "import tests.UpperCaseFunction;\n" +
                "\n" +
                "public class FrontFunction implements MessageMapper {\n" +
                "   public Message map(Message input) {\n" +
                "        return new UpperCaseFunction().map(input);\n" +
                "   }\n" +
                "}";
    }
}
