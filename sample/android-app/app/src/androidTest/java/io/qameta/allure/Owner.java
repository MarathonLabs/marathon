package io.qameta.allure;

import java.lang.annotation.*;

/**
 * Used to mark tests with owner label.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Owner {

    String value();

}
