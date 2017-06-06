package messages;

public class Messages {

    public static String MessageWithComponent =
        "[Person]\n" +
        "name: Jose\n" +
        "money: 50.0\n" +
        "birthDate: 2016-10-04T10:10:11Z\n" +
        "\n" +
        "[Person.Country]\n" +
        "name: Spain\n";

    public static String MessageWithParentClass =
        "[Teacher]\n" +
        "name: Jose\n" +
        "money: 50.0\n" +
        "birthDate: 2016-10-04T20:10:12Z\n" +
        "university: ULPGC\n" +
        "\n" +
        "[Teacher.Country]\n" +
        "name: Spain\n";


    public static String EmptyAttributeMessage =
        "[Teacher]\n" +
        "name: Jose\n" +
        "money: 50.0\n" +
        "birthDate: 2016-10-04T20:10:11Z\n" +
        "university: ULPGC\n" +
        "\n" +
        "[Person.Country]\n" +
        "name: Spain\n" +
        "continent:\n";

    public static String OldFormatMessage =
        "[Teacher]\n" +
        "name = \"Jose\"\n" +
        "money=50.0\n" +
        "birthDate= 2016-10-04T20:10:12Z\n" +
        "university = ULPGC\n" +
        "\n" +
        "[Teacher.Country]\n" +
        "name=\"Spain\"\n" +
        "continent=\n";

    public static String CsvMessages =
        "Date;  Time;  Global_active_power;Global_reactive_power;Voltage;Global_intensity;Sub_metering_1;Sub_metering_2;  Sub_metering_3\n" +
        "16/12/2006;17:24:00;4.216;0.418;234.840;18.400;0.000;1.000;17.000\n" +
        "16/12/2006;17:25:00;5.360;0.436;233.630;23.000;0.000;1.000;16.000\n" +
        "16/12/2006;17:26:00;5.374;0.498;233.290;23.000;0.000;2.000;17.000\n";

    public static String DatMessages =
        "building=01 class=01 room=HZG\n" + CsvMessages;


    public static String MultipleComponentMessage =
        "[Teacher]\n" +
        "name: Jose\n" +
        "money: 50.0\n" +
        "birthDate: 2016-10-04T20:10:11Z\n" +
        "university: ULPGC\n" +
        "\n" +
        "[Teacher.Country]\n" +
        "name: Spain\n" +
        "\n" +
        "[Teacher.Phone]\n" +
        "value: +150512101402\n" +
        "\n" +
        "[Teacher.Phone.Country]\n" +
        "name: USA\n" +
        "\n" +
        "[Teacher.Phone]\n" +
        "value: +521005101402\n" +
        "\n" +
        "[Teacher.Phone.Country]\n" +
        "name: Mexico\n";

    public static String MultiLineMessage =
        "[Teacher]\n" +
        "name:\n\tJose\n\tHernandez\n" +
        "money: 50.0\n" +
        "birthDate: 2016-10-04T20:10:11Z\n" +
        "university: ULPGC\n" +
        "\n" +
        "[Teacher.Country]\n" +
        "name: Spain\n" +
        "\n" +
        "[Teacher.Phone]\n" +
        "value: +150512101402\n" +
        "\n" +
        "[Teacher.Phone.Country]\n" +
        "name: USA\n" +
        "\n" +
        "[Teacher.Phone]\n" +
        "value: +521005101402\n" +
        "\n" +
        "[Teacher.Phone.Country]\n" +
        "name: Mexico\n";


    public static String Stack =
            "java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object java.util.List.get(int)' on a null object reference\n" +
                    "    at io.intino.consul.AppService$5.run(AppService.java:154)\n" +
                    "    at android.os.Handler.handleCallback(Handler.java:815)\n" +
                    "    at android.os.Handler.dispatchMessage(Handler.java:104)\n" +
                    "    at android.os.Looper.loop(Looper.java:194)\n" +
                    "    at android.app.ActivityThread.main(ActivityThread.java:5666)\n" +
                    "    at java.lang.reflect.Method.invoke(Native Method)\n" +
                    "    at java.lang.reflect.Method.invoke(Method.java:372)\n" +
                    "\n" +
                    "    at com.android.compiler.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:959)\n" +
                    "    at com.android.compiler.os.ZygoteInit.main(ZygoteInit.java:754)" +
					"\n" +
					"\n";


    public static String CrashMessage =
        "[Crash]\n" +
        "instant: 2017-03-21T07:39:00Z\n" +
        "app: io.intino.consul\n" +
        "deviceId: b367172b0c6fe726\n" +
        "stack:\n" + indent(Stack) + "\n";

    private static String Status1 =
            "[Status]\n" +
                    "battery: 78.0\n" +
                    "cpuUsage: 11.95\n" +
                    "isPlugged: true\n" +
                    "isScreenOn: false\n" +
                    "temperature: 29.0\n" +
                    "created: 2017-03-22T12:56:18Z\n";

    private static String Status2 =
            "[Status]\n" +
                    "battery: 78.0\n" +
                    "cpuUsage: 11.95\n" +
                    "isPlugged: true\n" +
                    "isScreenOn: true\n" +
                    "temperature: 29.0\n" +
                    "created: 2017-03-22T12:56:18Z\n";


    public static String StatusMessage =
        Status1 + "\n" + Status2;


    public static String MenuMessage =
        "[Menu]\n" +
        "meals:\n" +
        "\tSoup\n" +
        "\tLobster\n" +
        "\tMussels\n" +
        "\tCake\n" +
        "prices:\n" +
        "\t5.0\n" +
        "\t24.5\n" +
        "\t8.0\n" +
        "\t7.0\n" +
        "availability:\n" +
        "\ttrue\n" +
        "\tfalse\n";


    public static String EmptyMenuMessage =
        "[Menu]\n" +
        "availability:\n" +
        "\ttrue\n" +
        "\tfalse\n";


    public static String NullValueMenuMessage =
        "[Menu]\n" +
        "meals:\n" +
        "\tSoup\n" +
        "\t\0\n" +
        "\tMussels\n" +
        "\tCake\n" +
        "prices:\n" +
        "\t5.0\n" +
        "\t\0\n" +
        "\t8.0\n" +
        "\t7.0\n" +
        "availability:\n" +
        "\ttrue\n" +
        "\tfalse\n";

    private static String indent(String text) {
        return "\t" + text.replaceAll("\\n", "\n\t");
    }

}
