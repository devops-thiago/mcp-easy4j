package br.com.arquivolivre.mcpeasy4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an MCP server entry point. The framework will scan for all annotated methods in
 * the class and register them.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpServer {
  /**
   * The name of the MCP server.
   *
   * @return the server name, defaults to empty string (will use class name)
   */
  String name() default "";

  /**
   * The version of the MCP server.
   *
   * @return the server version, defaults to "1.0.0"
   */
  String version() default "1.0.0";

  /**
   * Whether to enable resource capabilities.
   *
   * @return true if resources should be enabled, defaults to true
   */
  boolean enableResources() default true;

  /**
   * Whether to enable prompt capabilities.
   *
   * @return true if prompts should be enabled, defaults to true
   */
  boolean enablePrompts() default true;
}
