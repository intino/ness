package schemas;

import java.util.Date;

public class Teacher extends Person {
    public String university;

    public Teacher() {
    }

    public Teacher(String name, double money, Date birthDate, Country country) {
        super(name, money, birthDate, country);
    }
}
