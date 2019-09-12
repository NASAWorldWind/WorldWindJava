package org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to define a non-static,
 * single-argument method, to be used as a "fallback" handler
 * for all otherwise unrecognized properties found from Json content.
 *<p>
 * If used, all otherwise unmapped key-value pairs from Json Object
 * structs are added to the property (of type Map or bean).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonAnySetter
{
}
