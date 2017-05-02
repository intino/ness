package io.intino.ness.konos.jmx;

import java.util.*;
import java.time.*;

public interface ManagerMBean {

    String addUser(String name, java.util.List<String> groups);

    String removeUser(String name);

    String addTank(String name);

    String removeTank(String name);

    java.util.List<String> users();

    java.util.List<String> tanks(java.util.List<String> tags);

    java.util.List<String> functions();

    java.util.List<String> topics();

    String rename(String tank, String name);

    String seal(String tank);

    String migrate(String tank, java.util.List<String> functions);

    String reflow(String tank);

    String addFunction(String name, String code);

    String pump(String functionName, String input, String output);
}