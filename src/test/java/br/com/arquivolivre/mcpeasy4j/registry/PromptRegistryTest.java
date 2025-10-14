package br.com.arquivolivre.mcpeasy4j.registry;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for PromptRegistry. */
class PromptRegistryTest {

  private PromptRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new PromptRegistry();
  }

  @Test
  void testRegisterAndGetPrompt() throws Exception {
    Method method = TestClass.class.getMethod("generatePrompt");
    PromptDefinition definition =
        new PromptDefinition(
            "testPrompt", "Test Prompt", "Description", List.of(), method, new TestClass());

    registry.register(definition);

    PromptDefinition retrieved = registry.getPrompt("testPrompt");
    assertNotNull(retrieved);
    assertEquals("testPrompt", retrieved.name());
    assertEquals("Test Prompt", retrieved.title());
  }

  @Test
  void testGetNonExistentPrompt() {
    PromptDefinition retrieved = registry.getPrompt("nonExistent");
    assertNull(retrieved);
  }

  @Test
  void testListPrompts() throws Exception {
    Method method = TestClass.class.getMethod("generatePrompt");

    PromptDefinition def1 =
        new PromptDefinition("prompt1", "Prompt 1", "Desc 1", List.of(), method, new TestClass());
    PromptDefinition def2 =
        new PromptDefinition("prompt2", "Prompt 2", "Desc 2", List.of(), method, new TestClass());

    registry.register(def1);
    registry.register(def2);

    List<PromptDefinition> prompts = registry.listPrompts();
    assertEquals(2, prompts.size());
  }

  static class TestClass {
    public String generatePrompt() {
      return "prompt text";
    }
  }
}
