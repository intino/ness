package io.intino.ness.inl;

class Attribute {
    String name;
    String value;

    Attribute() {

    }

    Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    Attribute parse(String line) {
        int i = line.indexOf(":");
        name = line.substring(0, i);
        value = i < line.length() - 1 ? unwrap(line.substring(i + 1)) : null;
        return this;
    }

    Attribute add(String line) {
        value = (value == null) ? line : value + "\n" + line;
        return this;
    }

    private String unwrap(String text) {
        return text.startsWith("\"") && text.endsWith("\"") ? text.substring(1, text.length() - 1) : text;
    }
    @Override
    public String toString() {
        return name + ": " + value;
    }
}
