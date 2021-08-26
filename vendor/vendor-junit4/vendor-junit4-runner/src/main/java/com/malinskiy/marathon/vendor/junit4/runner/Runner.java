package com.malinskiy.marathon.vendor.junit4.runner;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Runner {
    public static void main(String[] args) {
        Map<String, String> environ = System.getenv();
        Integer port = Integer.valueOf(environ.get("PORT"));
        File filterFile = new File(environ.get("FILTER"));
        List<String> tests = new ArrayList<>();

        try (Scanner scanner = new Scanner(filterFile)) {
            while (scanner.hasNextLine()) {
                tests.add(scanner.nextLine());
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Set<Class<?>> klasses = new HashSet<>();
        Set<Description> testDescriptions = new HashSet<>();

        tests.forEach(fqtn -> {
            String klass = fqtn.substring(0, fqtn.indexOf('#'));
            Class<?> testClass = Class.forName(klass);
            klasses.add(testClass);

            String method = fqtn.substring(fqtn.indexOf('#') + 1);
            testDescriptions.add(Description.createTestDescription(testClass, method));
        });

        Map<Description, String> actualClassLocator = new HashMap<>();
        TestFilter testFilter = new TestFilter(testDescriptions, actualClassLocator);

        Request request = Request.classes(klasses.toArray(new Class<?>[] {}))
            .filterWith(testFilter);

        JUnitCore core = new JUnitCore();
        ListenerAdapter adapter = new ListenerAdapter();
        try {
            core.addListener(adapter);
            core.run(request);
            core.removeListener(adapter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
