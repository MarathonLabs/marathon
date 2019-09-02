package io.qameta.allure;

/**
 * Used to mark tests with severity label.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Severity {

    SeverityLevel value();
}
