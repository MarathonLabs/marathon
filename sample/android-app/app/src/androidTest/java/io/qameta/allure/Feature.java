package io.qameta.allure;

import java.lang.annotation.*;

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
