package br.com.arquivolivre.mcpeasy4j.scanner;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.annotation.*;
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
    var tools = scanner.scanTools(testServer);

    assertNotNull(tools);
    assertEquals(2, tools.size());

    var echoTool = tools.stream().filter(t -> t.name().equals("echo")).findFirst().orElse(null);
    assertNotNull(echoTool);
    assertEquals("Echoes a message", echoTool.description());
  }

  @Test
  void testScanResources() {
    var resources = scanner.scanResources(testServer);

    assertNotNull(resources);
    assertEquals(1, resources.size());

    var resource = resources.get(0);
    assertEquals("file://test.txt", resource.uri());
    assertEquals("Test Resource", resource.title());
    assertEquals("text/plain", resource.mimeType());
  }

  @Test
  void testScanPrompts() {
    var prompts = scanner.scanPrompts(testServer);

    assertNotNull(prompts);
    assertEquals(1, prompts.size());

    var prompt = prompts.get(0);
    assertEquals("testPrompt", prompt.name());
    assertEquals("Test Prompt", prompt.title());
  }

  @Test
  void testScanToolsWithDefaultName() {
    var tools = scanner.scanTools(testServer);

    var addTool = tools.stream().filter(t -> t.name().equals("add")).findFirst().orElse(null);
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
