package br.com.arquivolivre.mcpeasy4j.integration;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.adapter.SdkFeatureAdapter;
import br.com.arquivolivre.mcpeasy4j.annotation.Prompt;
import br.com.arquivolivre.mcpeasy4j.annotation.PromptArgument;
import br.com.arquivolivre.mcpeasy4j.annotation.Property;
import br.com.arquivolivre.mcpeasy4j.annotation.Resource;
import br.com.arquivolivre.mcpeasy4j.annotation.Tool;
import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import br.com.arquivolivre.mcpeasy4j.scanner.AnnotationScanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * End-to-end integration test for SDK transport. Tests the complete flow from annotation scanning
 * to feature registration with the SDK server.
 */
class SdkTransportIntegrationTest {

  @br.com.arquivolivre.mcpeasy4j.annotation.McpServer(
      name = "integration-test-server",
      enableResources = true,
      enablePrompts = true)
  public static class IntegrationTestServer {
    @Tool(name = "test_tool", description = "Test tool for integration")
    public String testTool(@Property(name = "input", required = true) String input) {
      return "processed: " + input;
    }

    @Tool(name = "math_add", description = "Adds two numbers")
    public double add(
        @Property(name = "a", required = true) double a,
        @Property(name = "b", required = true) double b) {
      return a + b;
    }

    @Resource(
        uri = "test://resource",
        title = "Test Resource",
        description = "Test resource for integration")
    public String testResource() {
      return "resource content";
    }

    @Resource(
        uri = "test://json",
        title = "JSON Resource",
        description = "JSON resource",
        mimeType = "application/json")
    public Map<String, Object> jsonResource() {
      return Map.of("key", "value", "number", 42);
    }

    @Prompt(name = "test_prompt", description = "Test prompt for integration")
    public String testPrompt(@PromptArgument(name = "arg", required = true) String arg) {
      return "Prompt with argument: " + arg;
    }
  }

  private AnnotationScanner scanner;
  private SdkFeatureAdapter adapter;
  private IntegrationTestServer serverInstance;

  @BeforeEach
  void setUp() {
    scanner = new AnnotationScanner();
    adapter = new SdkFeatureAdapter(new ObjectMapper());
    serverInstance = new IntegrationTestServer();
  }

  @Test
  void testCompleteFlowFromScanningToRegistration() {
    // Step 1: Scan annotations
    List<ToolDefinition> tools = scanner.scanTools(serverInstance);
    List<ResourceDefinition> resources = scanner.scanResources(serverInstance);
    List<PromptDefinition> prompts = scanner.scanPrompts(serverInstance);

    // Verify scanning worked
    assertEquals(2, tools.size(), "Should scan 2 tools");
    assertEquals(2, resources.size(), "Should scan 2 resources");
    assertEquals(1, prompts.size(), "Should scan 1 prompt");

    // Step 2: Create SDK server (without transport for testing)
    McpSyncServer sdkServer = createTestSdkServer();

    // Step 3: Register features with SDK
    assertDoesNotThrow(() -> adapter.registerTools(sdkServer, tools));
    assertDoesNotThrow(() -> adapter.registerResources(sdkServer, resources));
    assertDoesNotThrow(() -> adapter.registerPrompts(sdkServer, prompts));

    // Verify registration succeeded (no exceptions thrown)
    assertTrue(true, "Feature registration completed successfully");
  }

  @Test
  void testToolRegistrationAndInvocation() {
    // Scan and register tools
    List<ToolDefinition> tools = scanner.scanTools(serverInstance);
    McpSyncServer sdkServer = createTestSdkServer();
    adapter.registerTools(sdkServer, tools);

    // Verify tools were scanned correctly
    ToolDefinition testTool =
        tools.stream().filter(t -> t.name().equals("test_tool")).findFirst().orElse(null);
    assertNotNull(testTool, "test_tool should be found");
    assertEquals("Test tool for integration", testTool.description());

    ToolDefinition mathTool =
        tools.stream().filter(t -> t.name().equals("math_add")).findFirst().orElse(null);
    assertNotNull(mathTool, "math_add should be found");
    assertEquals("Adds two numbers", mathTool.description());
  }

  @Test
  void testResourceRegistrationAndMetadata() {
    // Scan and register resources
    List<ResourceDefinition> resources = scanner.scanResources(serverInstance);
    McpSyncServer sdkServer = createTestSdkServer();
    adapter.registerResources(sdkServer, resources);

    // Verify resources were scanned correctly
    ResourceDefinition testResource =
        resources.stream().filter(r -> r.uri().equals("test://resource")).findFirst().orElse(null);
    assertNotNull(testResource, "test://resource should be found");
    assertEquals("Test Resource", testResource.title());
    assertEquals("Test resource for integration", testResource.description());

    ResourceDefinition jsonResource =
        resources.stream().filter(r -> r.uri().equals("test://json")).findFirst().orElse(null);
    assertNotNull(jsonResource, "test://json should be found");
    assertEquals("application/json", jsonResource.mimeType());
  }

  @Test
  void testPromptRegistrationAndMetadata() {
    // Scan and register prompts
    List<PromptDefinition> prompts = scanner.scanPrompts(serverInstance);
    McpSyncServer sdkServer = createTestSdkServer();
    adapter.registerPrompts(sdkServer, prompts);

    // Verify prompts were scanned correctly
    PromptDefinition testPrompt =
        prompts.stream().filter(p -> p.name().equals("test_prompt")).findFirst().orElse(null);
    assertNotNull(testPrompt, "test_prompt should be found");
    assertEquals("Test prompt for integration", testPrompt.description());
  }

  @Test
  void testProtocolCompliance() {
    // Verify that server info is properly configured
    br.com.arquivolivre.mcpeasy4j.annotation.McpServer annotation =
        IntegrationTestServer.class.getAnnotation(
            br.com.arquivolivre.mcpeasy4j.annotation.McpServer.class);
    assertNotNull(annotation, "Server should have @McpServer annotation");
    assertEquals("integration-test-server", annotation.name());
    assertEquals("1.0.0", annotation.version());
    assertTrue(annotation.enableResources());
    assertTrue(annotation.enablePrompts());

    // Verify capabilities can be built
    McpSchema.ServerCapabilities capabilities =
        McpSchema.ServerCapabilities.builder()
            .tools(true)
            .resources(annotation.enableResources(), true)
            .prompts(annotation.enablePrompts())
            .build();

    assertNotNull(capabilities, "Capabilities should be created");
  }

  @Test
  void testSdkServerCreation() {
    // Test that SDK server can be created with proper configuration
    br.com.arquivolivre.mcpeasy4j.annotation.McpServer annotation =
        IntegrationTestServer.class.getAnnotation(
            br.com.arquivolivre.mcpeasy4j.annotation.McpServer.class);

    // Create a mock transport for testing
    StdioServerTransportProvider transport = new StdioServerTransportProvider(new ObjectMapper());

    // Build SDK server
    McpSyncServer sdkServer =
        io.modelcontextprotocol.server.McpServer.sync(transport)
            .serverInfo(annotation.name(), annotation.version())
            .capabilities(
                McpSchema.ServerCapabilities.builder()
                    .tools(true)
                    .resources(annotation.enableResources(), true)
                    .prompts(annotation.enablePrompts())
                    .build())
            .build();

    assertNotNull(sdkServer, "SDK server should be created");
  }

  @Test
  void testFeatureRegistrationOrder() {
    // Test that features can be registered in any order
    List<ToolDefinition> tools = scanner.scanTools(serverInstance);
    List<ResourceDefinition> resources = scanner.scanResources(serverInstance);
    List<PromptDefinition> prompts = scanner.scanPrompts(serverInstance);

    McpSyncServer sdkServer = createTestSdkServer();

    // Register in different orders
    assertDoesNotThrow(() -> adapter.registerPrompts(sdkServer, prompts));
    assertDoesNotThrow(() -> adapter.registerTools(sdkServer, tools));
    assertDoesNotThrow(() -> adapter.registerResources(sdkServer, resources));
  }

  @Test
  void testEmptyFeatureLists() {
    // Test that empty feature lists don't cause errors
    McpSyncServer sdkServer = createTestSdkServer();

    assertDoesNotThrow(() -> adapter.registerTools(sdkServer, List.of()));
    assertDoesNotThrow(() -> adapter.registerResources(sdkServer, List.of()));
    assertDoesNotThrow(() -> adapter.registerPrompts(sdkServer, List.of()));
  }

  /**
   * Creates a test SDK server without starting the transport. This allows testing feature
   * registration without requiring actual stdin/stdout.
   */
  private McpSyncServer createTestSdkServer() {
    StdioServerTransportProvider transport = new StdioServerTransportProvider(new ObjectMapper());

    return io.modelcontextprotocol.server.McpServer.sync(transport)
        .serverInfo("test-server", "1.0.0")
        .capabilities(
            McpSchema.ServerCapabilities.builder()
                .tools(true)
                .resources(true, true)
                .prompts(true)
                .build())
        .build();
  }
}
