package io.septapulse.rook.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields in a configuration and for marking a constructor to be
 * called using reflection.
 * 
 * @author Eric Thill
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface Configurable {
	String comment() default "";

	String defaultValue() default "";
	
	String min() default "";

	String max() default "";

	String increment() default "";
}
