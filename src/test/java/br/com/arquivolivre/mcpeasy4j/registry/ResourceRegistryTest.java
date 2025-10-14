package br.com.arquivolivre.mcpeasy4j.registry;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for ResourceRegistry. */
class ResourceRegistryTest {

  private ResourceRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new ResourceRegistry();
  }

  @Test
  void testRegisterAndGetResource() throws Exception {
    Method method = TestClass.class.getMethod("getResource");
    ResourceDefinition definition =
        new ResourceDefinition(
            "file://test.txt",
            "Test Resource",
            "Description",
            "text/plain",
            method,
            new TestClass());

    registry.register(definition);

    ResourceDefinition retrieved = registry.getResource("file://test.txt");
    assertNotNull(retrieved);
    assertEquals("file://test.txt", retrieved.uri());
    assertEquals("Test Resource", retrieved.title());
  }

  @Test
  void testGetNonExistentResource() {
    ResourceDefinition retrieved = registry.getResource("file://nonexistent.txt");
    assertNull(retrieved);
  }

  @Test
  void testListResources() throws Exception {
    Method method = TestClass.class.getMethod("getResource");

    ResourceDefinition def1 =
        new ResourceDefinition(
            "file://test1.txt", "Resource 1", "Desc 1", "text/plain", method, new TestClass());
    ResourceDefinition def2 =
        new ResourceDefinition(
            "file://test2.txt", "Resource 2", "Desc 2", "text/plain", method, new TestClass());

    registry.register(def1);
    registry.register(def2);

    List<ResourceDefinition> resources = registry.listResources();
    assertEquals(2, resources.size());
  }

  @Test
  void testListTemplates() throws Exception {
    Method method = TestClass.class.getMethod("getResource");

    ResourceDefinition template =
        new ResourceDefinition(
            "file://{id}.txt",
            "Template",
            "Template resource",
            "text/plain",
            method,
            new TestClass());

    registry.register(template);

    List<ResourceDefinition> templates = registry.listTemplates();
    assertEquals(1, templates.size());
    assertTrue(templates.get(0).uri().contains("{"));
  }

  static class TestClass {
    public String getResource() {
      return "content";
    }
  }
}
