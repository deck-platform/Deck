package deck.stereotype;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DB {
    @AliasFor(
            annotation = Source.class
    )
    String value() default "";
}