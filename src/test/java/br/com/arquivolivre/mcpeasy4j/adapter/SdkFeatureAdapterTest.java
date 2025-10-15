package br.com.arquivolivre.mcpeasy4j.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.arquivolivre.mcpeasy4j.model.PromptArgument;
import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkFeatureAdapterTest {

  @Mock private McpSyncServer mockServer;

  private SdkFeatureAdapter adapter;
  private ObjectMapper objectMapper;
  private TestService testService;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    adapter = new SdkFeatureAdapter(objectMapper);
    testService = new TestService();
  }

  @Test
  void testRegisterTools() throws Exception {
    // Arrange
    var method = TestService.class.getMethod("echoTool", String.class);
    var schema =
        new McpSchema.JsonSchema(
            "object",
            Map.of("message", Map.of("type", "string")),
            List.of("message"),
            null,
            null,
            null);

    var toolDef = new ToolDefinition("echo", "Echo tool", schema, method, testService);

    // Act
    adapter.registerTools(mockServer, List.of(toolDef));

    // Assert
    var specCaptor = ArgumentCaptor.forClass(SyncToolSpecification.class);
    verify(mockServer).addTool(specCaptor.capture());

    var capturedSpec = specCaptor.getValue();
    assertEquals("echo", capturedSpec.tool().name());
    assertEquals("Echo tool", capturedSpec.tool().description());
    assertEquals(schema, capturedSpec.tool().inputSchema());
  }

  @Test
  void testToolCallbackInvocation() throws Exception {
    // Arrange
    var method = TestService.class.getMethod("echoTool", String.class);
    var schema = new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null);

    var toolDef = new ToolDefinition("echo", "Echo tool", schema, method, testService);

    adapter.registerTools(mockServer, List.of(toolDef));

    var specCaptor = ArgumentCaptor.forClass(SyncToolSpecification.class);
    verify(mockServer).addTool(specCaptor.capture());

    var spec = specCaptor.getValue();

    // Act
    Map<String, Object> params = Map.of("message", "Hello");
    var result = spec.call().apply(null, params);

    // Assert
    assertNotNull(result);
    assertFalse(result.isError());
    assertEquals(1, result.content().size());
    assertTrue(result.content().get(0) instanceof TextContent);
    var content = (TextContent) result.content().get(0);
    assertEquals("Echo: Hello", content.text());
  }

  @Test
  void testRegisterResources() throws Exception {
    // Arrange
    Method method = TestService.class.getMethod("getResource");
    ResourceDefinition resourceDef =
        new ResourceDefinition(
            "file:///test.txt", "Test File", "A test file", "text/plain", method, testService);

    // Act
    adapter.registerResources(mockServer, List.of(resourceDef));

    // Assert
    ArgumentCaptor<SyncResourceSpecification> specCaptor =
        ArgumentCaptor.forClass(SyncResourceSpecification.class);
    verify(mockServer).addResource(specCaptor.capture());

    SyncResourceSpecification capturedSpec = specCaptor.getValue();
    assertEquals("file:///test.txt", capturedSpec.resource().uri());
    assertEquals("Test File", capturedSpec.resource().name());
    assertEquals("A test file", capturedSpec.resource().description());
    assertEquals("text/plain", capturedSpec.resource().mimeType());
  }

  @Test
  void testResourceCallbackInvocation() throws Exception {
    // Arrange
    Method method = TestService.class.getMethod("getResource");
    ResourceDefinition resourceDef =
        new ResourceDefinition(
            "file:///test.txt", "Test File", "A test file", "text/plain", method, testService);

    adapter.registerResources(mockServer, List.of(resourceDef));

    ArgumentCaptor<SyncResourceSpecification> specCaptor =
        ArgumentCaptor.forClass(SyncResourceSpecification.class);
    verify(mockServer).addResource(specCaptor.capture());

    SyncResourceSpecification spec = specCaptor.getValue();

    // Act
    ReadResourceResult result = spec.readHandler().apply(null, null);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.contents().size());
    assertTrue(result.contents().get(0) instanceof TextResourceContents);
    TextResourceContents content = (TextResourceContents) result.contents().get(0);
    assertEquals("Resource content", content.text());
    assertEquals("file:///test.txt", content.uri());
    assertEquals("text/plain", content.mimeType());
  }

  @Test
  void testRegisterPrompts() throws Exception {
    // Arrange
    Method method = TestService.class.getMethod("generatePrompt", String.class);
    List<PromptArgument> arguments =
        List.of(new PromptArgument("topic", "The topic to generate prompt for", true));

    PromptDefinition promptDef =
        new PromptDefinition(
            "generate", "Generate Prompt", "Generates a prompt", arguments, method, testService);

    // Act
    adapter.registerPrompts(mockServer, List.of(promptDef));

    // Assert
    ArgumentCaptor<SyncPromptSpecification> specCaptor =
        ArgumentCaptor.forClass(SyncPromptSpecification.class);
    verify(mockServer).addPrompt(specCaptor.capture());

    SyncPromptSpecification capturedSpec = specCaptor.getValue();
    assertEquals("generate", capturedSpec.prompt().name());
    assertEquals("Generate Prompt", capturedSpec.prompt().title());
    assertEquals("Generates a prompt", capturedSpec.prompt().description());
    assertEquals(1, capturedSpec.prompt().arguments().size());
    assertEquals("topic", capturedSpec.prompt().arguments().get(0).name());
  }

  @Test
  void testPromptCallbackInvocation() throws Exception {
    // Arrange
    Method method = TestService.class.getMethod("generatePrompt", String.class);
    List<PromptArgument> arguments = List.of(new PromptArgument("topic", "The topic", true));

    PromptDefinition promptDef =
        new PromptDefinition(
            "generate", "Generate Prompt", "Generates a prompt", arguments, method, testService);

    adapter.registerPrompts(mockServer, List.of(promptDef));

    ArgumentCaptor<SyncPromptSpecification> specCaptor =
        ArgumentCaptor.forClass(SyncPromptSpecification.class);
    verify(mockServer).addPrompt(specCaptor.capture());

    SyncPromptSpecification spec = specCaptor.getValue();

    // Create mock request
    GetPromptRequest request = new GetPromptRequest("generate", Map.of("topic", "AI"));

    // Act
    GetPromptResult result = spec.promptHandler().apply(null, request);

    // Assert
    assertNotNull(result);
    assertEquals("Generates a prompt", result.description());
    assertEquals(1, result.messages().size());
    McpSchema.PromptMessage message = result.messages().get(0);
    assertEquals(Role.USER, message.role());
    assertTrue(message.content() instanceof TextContent);
    TextContent content = (TextContent) message.content();
    assertEquals("Prompt about: AI", content.text());
  }

  @Test
  void testToolCallbackWithComplexObject() throws Exception {
    // Arrange
    Method method = TestService.class.getMethod("complexTool", Map.class);
    McpSchema.JsonSchema schema =
        new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null);

    ToolDefinition toolDef =
        new ToolDefinition("complex", "Complex tool", schema, method, testService);

    adapter.registerTools(mockServer, List.of(toolDef));

    ArgumentCaptor<SyncToolSpecification> specCaptor =
        ArgumentCaptor.forClass(SyncToolSpecification.class);
    verify(mockServer).addTool(specCaptor.capture());

    SyncToolSpecification spec = specCaptor.getValue();

    // Act
    Map<String, Object> params = Map.of("data", Map.of("key", "value"));
    CallToolResult result = spec.call().apply(null, params);

    // Assert
    assertNotNull(result);
    TextContent content = (TextContent) result.content().get(0);
    assertTrue(content.text().contains("key"));
    assertTrue(content.text().contains("value"));
  }

  @Test
  void testToolCallbackWithNullResult() throws Exception {
    // Arrange
    Method method = TestService.class.getMethod("nullTool");
    McpSchema.JsonSchema schema =
        new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null);

    ToolDefinition toolDef = new ToolDefinition("null", "Null tool", schema, method, testService);

    adapter.registerTools(mockServer, List.of(toolDef));

    ArgumentCaptor<SyncToolSpecification> specCaptor =
        ArgumentCaptor.forClass(SyncToolSpecification.class);
    verify(mockServer).addTool(specCaptor.capture());

    SyncToolSpecification spec = specCaptor.getValue();

    // Act
    CallToolResult result = spec.call().apply(null, Map.of());

    // Assert
    assertNotNull(result);
    TextContent content = (TextContent) result.content().get(0);
    assertEquals("", content.text());
  }

  @Test
  void testMultipleToolsRegistration() throws Exception {
    // Arrange
    Method method1 = TestService.class.getMethod("echoTool", String.class);
    Method method2 = TestService.class.getMethod("nullTool");
    McpSchema.JsonSchema schema =
        new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null);

    ToolDefinition tool1 = new ToolDefinition("echo", "Echo", schema, method1, testService);
    ToolDefinition tool2 = new ToolDefinition("null", "Null", schema, method2, testService);

    // Act
    adapter.registerTools(mockServer, List.of(tool1, tool2));

    // Assert
    verify(mockServer, times(2)).addTool(any(SyncToolSpecification.class));
  }

  // Test service class with methods to be invoked
  public static class TestService {
    public String echoTool(String message) {
      return "Echo: " + message;
    }

    public String getResource() {
      return "Resource content";
    }

    public String generatePrompt(String topic) {
      return "Prompt about: " + topic;
    }

    public Map<String, Object> complexTool(Map<String, Object> data) {
      return Map.of("result", data);
    }

    public String nullTool() {
      return null;
    }
  }
}
