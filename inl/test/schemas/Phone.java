package schemas;

public class Phone {
    public String value;
    public Country country;

    public Phone() {
    }

    public Phone(String value, Country country) {
        this.value = value;
        this.country = country;
    }
}
