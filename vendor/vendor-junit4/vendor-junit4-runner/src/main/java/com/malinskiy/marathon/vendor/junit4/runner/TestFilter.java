package com.malinskiy.marathon.vendor.junit4.runner;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Map;
import java.util.Set;

public class TestFilter extends Filter {
    private Set<Description> descriptions;
    private Map<Description, String> classLocator;

    public TestFilter(Set<Description> descriptions, Map<Description, String> classLocator) {
        this.descriptions = descriptions;
        this.classLocator = classLocator;
    }

    @Override
    public boolean shouldRun(Description description) {
        return false;
    }

    @Override
    public String describe() {
        return null;
    }
}
