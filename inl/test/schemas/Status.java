package schemas;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Status {
    public double battery;
    public double cpuUsage;
    public boolean isPlugged;
    public boolean isScreenOn;
    public double temperature;
    public Date created;

    public Status temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Status battery(double battery) {
        this.battery = battery;
        return this;
    }

    public Status isPlugged(boolean isPlugged) {
        this.isPlugged = isPlugged;
        return this;
    }

    public Status isScreenOn(boolean isScreenOn) {
        this.isScreenOn = isScreenOn;
        return this;
    }

    public Status cpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
        return this;
    }

    public boolean isScreenOn() {
        return isScreenOn;
    }

    public Date created() {
        return created;
    }

    public Status created(String date) {
        created = parseDate(date);
        return this;
    }

    private static Date parseDate(final String inputDateAsString) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        try {
            return format.parse(inputDateAsString);
        } catch (ParseException e) {
            return null;
        }
    }

}
