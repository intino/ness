package schemas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Person {
    public String name;
    public double money;
    public Date birthDate;
    public Country country;
    public List<Phone> phones;

    public Person() {
    }

    public Person(String name, double money, Date birthDate, Country country) {
        this.name = name;
        this.money = money;
        this.birthDate = birthDate;
        this.country = country;
    }

    public void add(Phone phone) {
        if (phones == null) phones = new ArrayList<>();
        phones.add(phone);
    }
}
