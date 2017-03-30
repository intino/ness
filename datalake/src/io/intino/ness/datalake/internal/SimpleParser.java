package io.intino.ness.datalake.internal;

public class SimpleParser {
    private String source;

    public static String qualifiedClassNameIn(String source) {
        return new SimpleParser(source).getQualifiedClassName();
    }

    private SimpleParser(String source) {
        this.source = source;
    }
    private String getQualifiedClassName() {
        skip("package ");
        String packageName = nextToken(";");
        skip("class ");
        return packageName + "." + nextToken("[\\s|\\{]");
    }

    private String nextToken(String separators) {
        return source.split(separators)[0];
    }

    private String skip(String text) {
        String result = source.substring(0, source.indexOf(text));
        source = source.substring(source.indexOf(text)+text.length());
        return result;
    }

}
