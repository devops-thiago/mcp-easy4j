package br.com.arquivolivre.mcpeasy4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a method parameter as a tool property. Used to define the input schema for MCP tools. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
  /**
   * The name of the property.
   *
   * @return the property name, defaults to empty string (will use parameter name)
   */
  String name() default "";

  /**
   * A description of the property.
   *
   * @return the property description, defaults to empty string
   */
  String description() default "";

  /**
   * Whether the property is required.
   *
   * @return true if the property is required, defaults to true
   */
  boolean required() default true;

  /**
   * The format constraint for the property (e.g., "email", "uri", "date-time").
   *
   * @return the format constraint, defaults to empty string
   */
  String format() default "";
}
