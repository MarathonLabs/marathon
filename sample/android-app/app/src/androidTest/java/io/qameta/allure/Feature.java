package io.qameta.allure;

/**
 * Used to mark tests with feature label.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Feature {

    String value();
}
