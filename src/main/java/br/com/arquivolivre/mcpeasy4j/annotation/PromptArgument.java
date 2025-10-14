package br.com.arquivolivre.mcpeasy4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as a prompt argument. Used to define the argument schema for MCP
 * prompts.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PromptArgument {
  /**
   * The name of the argument.
   *
   * @return the argument name, defaults to empty string (will use parameter name)
   */
  String name() default "";

  /**
   * A description of the argument.
   *
   * @return the argument description, defaults to empty string
   */
  String description() default "";

  /**
   * Whether the argument is required.
   *
   * @return true if the argument is required, defaults to true
   */
  boolean required() default true;
}
