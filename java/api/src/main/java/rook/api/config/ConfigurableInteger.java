package rook.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields in a configuration with context about allowed 64 bit
 * integer values
 * 
 * @author Eric Thill
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurableInteger {
	String comment() default "";

	long min() default 0;

	long max() default 0;

	long increment() default 0;

	String defaultValue() default "";
}
