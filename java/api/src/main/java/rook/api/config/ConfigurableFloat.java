package rook.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields in a configuration with context about allowed IEEE
 * floating point values
 * 
 * @author Eric Thill
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurableFloat {
	String comment() default "";

	double min() default 0;

	double max() default 0;

	double increment() default 0;
}
