package br.com.arquivolivre.mcpeasy4j.registry;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for ToolRegistry. */
class ToolRegistryTest {

  private ToolRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new ToolRegistry();
  }

  @Test
  void testRegisterAndGetTool() throws Exception {
    Method method = TestClass.class.getMethod("testMethod");
    McpSchema.JsonSchema schema = new McpSchema.JsonSchema("object", null, null, null, null, null);
    ToolDefinition definition =
        new ToolDefinition("testTool", "Test description", schema, method, new TestClass());

    registry.register(definition);

    ToolDefinition retrieved = registry.getTool("testTool");
    assertNotNull(retrieved);
    assertEquals("testTool", retrieved.name());
    assertEquals("Test description", retrieved.description());
  }

  @Test
  void testGetNonExistentTool() {
    ToolDefinition retrieved = registry.getTool("nonExistent");
    assertNull(retrieved);
  }

  @Test
  void testListTools() throws Exception {
    Method method1 = TestClass.class.getMethod("testMethod");
    Method method2 = TestClass.class.getMethod("anotherMethod");
    McpSchema.JsonSchema schema = new McpSchema.JsonSchema("object", null, null, null, null, null);

    ToolDefinition def1 =
        new ToolDefinition("tool1", "Description 1", schema, method1, new TestClass());
    ToolDefinition def2 =
        new ToolDefinition("tool2", "Description 2", schema, method2, new TestClass());

    registry.register(def1);
    registry.register(def2);

    List<ToolDefinition> tools = registry.listTools();
    assertEquals(2, tools.size());
    assertTrue(tools.stream().anyMatch(t -> t.name().equals("tool1")));
    assertTrue(tools.stream().anyMatch(t -> t.name().equals("tool2")));
  }

  @Test
  void testRegisterDuplicateTool() throws Exception {
    Method method = TestClass.class.getMethod("testMethod");
    McpSchema.JsonSchema schema = new McpSchema.JsonSchema("object", null, null, null, null, null);
    ToolDefinition def1 =
        new ToolDefinition("sameName", "Description 1", schema, method, new TestClass());
    ToolDefinition def2 =
        new ToolDefinition("sameName", "Description 2", schema, method, new TestClass());

    registry.register(def1);
    registry.register(def2);

    // Second registration should override
    ToolDefinition retrieved = registry.getTool("sameName");
    assertEquals("Description 2", retrieved.description());
  }

  static class TestClass {
    public void testMethod() {}

    public void anotherMethod() {}
  }
}
