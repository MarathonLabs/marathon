package io.qameta.allure;

import java.lang.annotation.*;

/**
 * Used to mark tests with story label.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Story {

    String value();

}
