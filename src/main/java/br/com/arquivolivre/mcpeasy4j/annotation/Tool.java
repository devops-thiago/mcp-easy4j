package br.com.arquivolivre.mcpeasy4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP tool. The method will be registered and callable via the tools/call MCP
 * protocol method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
  /**
   * The name of the tool.
   *
   * @return the tool name, defaults to empty string (will use method name)
   */
  String name() default "";

  /**
   * A description of what the tool does.
   *
   * @return the tool description, defaults to empty string
   */
  String description() default "";
}
