package br.com.arquivolivre.mcpeasy4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP prompt. The method will be registered and accessible via the prompts/get
 * MCP protocol method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Prompt {
  /**
   * The name of the prompt.
   *
   * @return the prompt name, defaults to empty string (will use method name)
   */
  String name() default "";

  /**
   * The title of the prompt.
   *
   * @return the prompt title, defaults to empty string
   */
  String title() default "";

  /**
   * A description of the prompt.
   *
   * @return the prompt description, defaults to empty string
   */
  String description() default "";
}
