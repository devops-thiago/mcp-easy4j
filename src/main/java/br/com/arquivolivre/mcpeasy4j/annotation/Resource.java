package br.com.arquivolivre.mcpeasy4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP resource. The method will be registered and accessible via the
 * resources/read MCP protocol method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {
  /**
   * The URI of the resource.
   *
   * @return the resource URI (required)
   */
  String uri();

  /**
   * The title of the resource.
   *
   * @return the resource title, defaults to empty string
   */
  String title() default "";

  /**
   * A description of the resource.
   *
   * @return the resource description, defaults to empty string
   */
  String description() default "";

  /**
   * The MIME type of the resource.
   *
   * @return the MIME type, defaults to "text/plain"
   */
  String mimeType() default "text/plain";
}
