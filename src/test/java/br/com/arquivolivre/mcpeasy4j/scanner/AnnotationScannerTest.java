package br.com.arquivolivre.mcpeasy4j.scanner;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.annotation.*;
import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for AnnotationScanner. */
class AnnotationScannerTest {

  private AnnotationScanner scanner;
  private TestServer testServer;

  @BeforeEach
  void setUp() {
    scanner = new AnnotationScanner();
    testServer = new TestServer();
  }

  @Test
  void testScanTools() {
    List<ToolDefinition> tools = scanner.scanTools(testServer);

    assertNotNull(tools);
    assertEquals(2, tools.size());

    ToolDefinition echoTool =
        tools.stream().filter(t -> t.name().equals("echo")).findFirst().orElse(null);
    assertNotNull(echoTool);
    assertEquals("Echoes a message", echoTool.description());
  }

  @Test
  void testScanResources() {
    List<ResourceDefinition> resources = scanner.scanResources(testServer);

    assertNotNull(resources);
    assertEquals(1, resources.size());

    ResourceDefinition resource = resources.get(0);
    assertEquals("file://test.txt", resource.uri());
    assertEquals("Test Resource", resource.title());
    assertEquals("text/plain", resource.mimeType());
  }

  @Test
  void testScanPrompts() {
    List<PromptDefinition> prompts = scanner.scanPrompts(testServer);

    assertNotNull(prompts);
    assertEquals(1, prompts.size());

    PromptDefinition prompt = prompts.get(0);
    assertEquals("testPrompt", prompt.name());
    assertEquals("Test Prompt", prompt.title());
  }

  @Test
  void testScanToolsWithDefaultName() {
    List<ToolDefinition> tools = scanner.scanTools(testServer);

    ToolDefinition addTool =
        tools.stream().filter(t -> t.name().equals("add")).findFirst().orElse(null);
    assertNotNull(addTool);
    // Name should default to method name
    assertEquals("add", addTool.name());
  }

  static class TestServer {
    @Tool(name = "echo", description = "Echoes a message")
    public String echo(@Property(name = "message") String message) {
      return message;
    }

    @Tool(description = "Adds two numbers")
    public double add(@Property(name = "a") double a, @Property(name = "b") double b) {
      return a + b;
    }

    @Resource(
        uri = "file://test.txt",
        title = "Test Resource",
        description = "A test resource",
        mimeType = "text/plain")
    public String getResource() {
      return "content";
    }

    @Prompt(name = "testPrompt", title = "Test Prompt", description = "A test prompt")
    public String generatePrompt(@PromptArgument(name = "input") String input) {
      return "Prompt: " + input;
    }
  }
}
