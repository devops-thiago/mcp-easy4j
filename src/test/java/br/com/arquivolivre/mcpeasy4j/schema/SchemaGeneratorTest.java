package br.com.arquivolivre.mcpeasy4j.schema;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.annotation.Property;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/** Tests for SchemaGenerator. */
class SchemaGeneratorTest {

  private final SchemaGenerator generator = new SchemaGenerator();

  @Test
  void testMapJavaTypeToJsonType_String() {
    assertEquals("string", generator.mapJavaTypeToJsonType(String.class));
  }

  @Test
  void testMapJavaTypeToJsonType_Integer() {
    assertEquals("integer", generator.mapJavaTypeToJsonType(int.class));
    assertEquals("integer", generator.mapJavaTypeToJsonType(Integer.class));
    assertEquals("integer", generator.mapJavaTypeToJsonType(long.class));
    assertEquals("integer", generator.mapJavaTypeToJsonType(Long.class));
  }

  @Test
  void testMapJavaTypeToJsonType_Number() {
    assertEquals("number", generator.mapJavaTypeToJsonType(double.class));
    assertEquals("number", generator.mapJavaTypeToJsonType(Double.class));
    assertEquals("number", generator.mapJavaTypeToJsonType(float.class));
    assertEquals("number", generator.mapJavaTypeToJsonType(Float.class));
  }

  @Test
  void testMapJavaTypeToJsonType_Boolean() {
    assertEquals("boolean", generator.mapJavaTypeToJsonType(boolean.class));
    assertEquals("boolean", generator.mapJavaTypeToJsonType(Boolean.class));
  }

  @Test
  void testMapJavaTypeToJsonType_Object() {
    assertEquals("object", generator.mapJavaTypeToJsonType(Object.class));
    assertEquals("object", generator.mapJavaTypeToJsonType(java.util.Map.class));
  }

  @Test
  void testMapJavaTypeToJsonType_Array() {
    assertEquals("array", generator.mapJavaTypeToJsonType(java.util.List.class));
    assertEquals("array", generator.mapJavaTypeToJsonType(String[].class));
  }

  @Test
  void testGenerateSchema() throws Exception {
    Method method = TestClass.class.getMethod("testMethod", String.class, int.class, boolean.class);
    McpSchema.JsonSchema schema = generator.generateSchema(method);

    assertNotNull(schema);
    assertEquals("object", schema.type());
    assertNotNull(schema.properties());
    assertTrue(schema.properties().containsKey("name"));
    assertTrue(schema.properties().containsKey("age"));
    assertTrue(schema.properties().containsKey("active"));
  }

  @Test
  void testGenerateSchema_RequiredFields() throws Exception {
    Method method = TestClass.class.getMethod("testMethodWithOptional", String.class, String.class);
    McpSchema.JsonSchema schema = generator.generateSchema(method);

    assertNotNull(schema.required());
    assertTrue(schema.required().contains("required"));
    assertFalse(schema.required().contains("optional"));
  }

  static class TestClass {
    public void testMethod(
        @Property(name = "name", description = "Name field") String name,
        @Property(name = "age", description = "Age field") int age,
        @Property(name = "active", description = "Active flag") boolean active) {}

    public void testMethodWithOptional(
        @Property(name = "required", description = "Required field", required = true)
            String required,
        @Property(name = "optional", description = "Optional field", required = false)
            String optional) {}
  }
}
