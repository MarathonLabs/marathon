package com.malinskiy.marathon.vendor.junit4.runner;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestFilter extends Filter {
    private Set<Description> descriptions;
    private Map<Description, String> classLocator;
    final private String CLASS_NAME_STUB = "org.junit.runners.model.TestClass";
    private Set<Description> verifiedChildren;

    public TestFilter(Set<Description> descriptions, Map<Description, String> classLocator) {
        this.descriptions = descriptions;
        this.classLocator = classLocator;
        verifiedChildren = new HashSet<>();
    }

    @Override
    public boolean shouldRun(Description description) {
        System.out.println("JUnit asks about $description");

        if (verifiedChildren.contains(description)) {
            return true;
        }
        else {
            return shouldRun(description, null);
        }
    }

    private boolean shouldRun(Description description, String className) {
        if (description.isTest()) {
            /**
             * Handling for parameterized tests that report org.junit.runners.model.TestClass as their test class
             */
            Description verificationDescription;
            if (description.getClassName().equals(CLASS_NAME_STUB) && className != null) {
                verificationDescription =
                    Description.createTestDescription(className, description.getMethodName(), description.getAnnotations().toArray());
            }
            else {
                verificationDescription = description;
            }
            Boolean contains = descriptions.contains(verificationDescription);
            if (contains) {
                verifiedChildren.add(description);
                if (description.getClassName().equals(CLASS_NAME_STUB) && className != null) {
                    classLocator.put(description, className);
                }
            }

            return contains;
        }

        // explicitly check if any children want to run
        Boolean childrenResult = false;
        for (Description each : description.getChildren()) {
            if (shouldRun(each, description.getClassName())) {
                childrenResult = true;
            }
        }

        return childrenResult;
    }

    @Override
    public String describe() {
        return "Marathon JUnit4 execution filter";
    }
}
