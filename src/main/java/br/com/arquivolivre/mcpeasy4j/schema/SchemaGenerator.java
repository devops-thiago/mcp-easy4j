package br.com.arquivolivre.mcpeasy4j.schema;

import br.com.arquivolivre.mcpeasy4j.annotation.Property;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

/**
 * Generates JSON schemas from Java method signatures. Uses Java 21 pattern matching for type
 * mapping and MCP SDK's JsonSchema.
 */
public class SchemaGenerator {

  /**
   * Maps a Java type to its corresponding JSON Schema type. Uses pattern matching for switch (Java
   * 21) to simplify type checking.
   *
   * @param type the Java class type
   * @return the JSON Schema type string
   */
  public String mapJavaTypeToJsonType(Class<?> type) {
    return switch (type) {
      case Class<?> c when c == String.class -> "string";
      case Class<?> c when c == int.class || c == Integer.class -> "integer";
      case Class<?> c when c == long.class || c == Long.class -> "integer";
      case Class<?> c when c == double.class || c == Double.class -> "number";
      case Class<?> c when c == float.class || c == Float.class -> "number";
      case Class<?> c when c == boolean.class || c == Boolean.class -> "boolean";
      case Class<?> c when c == Object.class -> "object";
      case Class<?> c when Map.class.isAssignableFrom(c) -> "object";
      case Class<?> c when List.class.isAssignableFrom(c) -> "array";
      case Class<?> c when c.isArray() -> "array";
      default -> "object"; // Default to object for unknown types
    };
  }

  /**
   * Generates a complete JSON schema from a method's parameters. Extracts @Property annotations and
   * builds property definitions. Uses SequencedMap to maintain parameter order. Returns MCP SDK's
   * JsonSchema for protocol compliance.
   *
   * @param method the method to generate schema for
   * @return the complete McpSchema.JsonSchema record
   */
  public McpSchema.JsonSchema generateSchema(Method method) {
    SequencedMap<String, Object> properties = new LinkedHashMap<>();
    var required = new ArrayList<String>();

    var parameters = method.getParameters();

    for (var parameter : parameters) {
      var propertyAnnotation = parameter.getAnnotation(Property.class);

      if (propertyAnnotation != null) {
        // Map Java type to JSON type
        var jsonType = mapJavaTypeToJsonType(parameter.getType());

        // Extract property name from annotation or parameter name
        final var propertyName =
            propertyAnnotation.name().isEmpty() ? parameter.getName() : propertyAnnotation.name();

        // Build property schema as a Map
        var propertySchema = new LinkedHashMap<String, Object>();
        propertySchema.put("type", jsonType);

        // Add description if present
        if (!propertyAnnotation.description().isEmpty()) {
          propertySchema.put("description", propertyAnnotation.description());
        }

        // Add format if present
        if (!propertyAnnotation.format().isEmpty()) {
          propertySchema.put("format", propertyAnnotation.format());
        }

        properties.put(propertyName, propertySchema);

        // Add to required list if marked as required
        if (propertyAnnotation.required()) {
          required.add(propertyName);
        }
      }
    }

    // Create MCP SDK JsonSchema
    return new McpSchema.JsonSchema(
        "object",
        properties,
        required,
        null, // additionalProperties
        null, // defs
        null // definitions
        );
  }
}
