package br.com.arquivolivre.mcpeasy4j;

import br.com.arquivolivre.mcpeasy4j.adapter.SdkFeatureAdapter;
import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import br.com.arquivolivre.mcpeasy4j.scanner.AnnotationScanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;

/**
 * Bootstrap class for initializing and starting MCP servers. Handles server lifecycle management
 * including scanning, registration, startup, and shutdown.
 */
public class McpServerBootstrap {

  /**
   * Starts an MCP server from the specified server class. Validates that the class has @McpServer
   * annotation and creates an instance.
   *
   * @param serverClass the class annotated with @McpServer
   * @throws IllegalArgumentException if the class is not annotated with @McpServer
   * @throws RuntimeException if the server instance cannot be created
   */
  public static void start(Class<?> serverClass) {
    // Validate that class has @McpServer annotation
    br.com.arquivolivre.mcpeasy4j.annotation.McpServer annotation =
        serverClass.getAnnotation(br.com.arquivolivre.mcpeasy4j.annotation.McpServer.class);
    if (annotation == null) {
      throw new IllegalArgumentException(
          "Class " + serverClass.getName() + " must be annotated with @McpServer");
    }

    // Create instance of server class
    Object serverInstance;
    try {
      serverInstance = serverClass.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(
          "Failed to create instance of " + serverClass.getName() + ": " + e.getMessage(), e);
    }

    // Create bootstrap instance and initialize server
    McpServerBootstrap bootstrap = new McpServerBootstrap(serverInstance, annotation);
    bootstrap.initialize();
  }

  private final Object serverInstance;
  private final br.com.arquivolivre.mcpeasy4j.annotation.McpServer annotation;
  private McpSyncServer sdkServer;
  private StdioServerTransportProvider transport;

  private McpServerBootstrap(
      Object serverInstance, br.com.arquivolivre.mcpeasy4j.annotation.McpServer annotation) {
    this.serverInstance = serverInstance;
    this.annotation = annotation;
  }

  private void initialize() {
    // Create SDK server instance
    createSdkServer();

    // Scan and register annotated methods
    scanAndRegister();

    // Start the server
    startServer();
  }

  /**
   * Creates the SDK server instance with server info from @McpServer annotation. Creates the
   * transport first, then builds the server with it.
   */
  private void createSdkServer() {
    // Create transport with ObjectMapper
    transport = new StdioServerTransportProvider(new ObjectMapper());

    // Build SDK server with transport
    sdkServer =
        McpServer.sync(transport)
            .serverInfo(annotation.name(), annotation.version())
            .capabilities(
                McpSchema.ServerCapabilities.builder()
                    .tools(true)
                    .resources(annotation.enableResources(), true)
                    .prompts(annotation.enablePrompts())
                    .build())
            .build();
  }

  /**
   * Scans the server instance for annotated methods and registers them with the SDK server. Creates
   * AnnotationScanner, scans for tools, resources, and prompts, and uses SdkFeatureAdapter to
   * register them with the SDK.
   */
  private void scanAndRegister() {
    // Create AnnotationScanner instance
    AnnotationScanner scanner = new AnnotationScanner();

    // Scan for tools
    List<ToolDefinition> tools = scanner.scanTools(serverInstance);

    // Scan for resources (if enabled)
    List<ResourceDefinition> resources = List.of();
    if (annotation.enableResources()) {
      resources = scanner.scanResources(serverInstance);
    }

    // Scan for prompts (if enabled)
    List<PromptDefinition> prompts = List.of();
    if (annotation.enablePrompts()) {
      prompts = scanner.scanPrompts(serverInstance);
    }

    // Register features with SDK using adapter (reuse the same ObjectMapper as transport)
    SdkFeatureAdapter adapter = new SdkFeatureAdapter(new ObjectMapper());
    adapter.registerTools(sdkServer, tools);
    adapter.registerResources(sdkServer, resources);
    adapter.registerPrompts(sdkServer, prompts);
  }

  /**
   * Starts the MCP server using SDK's StdioServerTransportProvider. The transport was already
   * created and wired during server creation, so we just need to register shutdown hook. The server
   * starts automatically when built.
   */
  private void startServer() {
    // Register shutdown hook for graceful shutdown
    registerShutdownHook();

    // Server is already running (started during build), just keep process alive
    // The transport blocks in the SDK server's message loop
    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Registers a shutdown hook for graceful shutdown. Handles Ctrl+D (EOF) and other shutdown
   * signals. Stops SDK transport, cleans up resources, and exits application.
   */
  private void registerShutdownHook() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  shutdown();
                }));
  }

  /** Performs graceful shutdown of the server. Stops SDK transport and cleans up resources. */
  private void shutdown() {
    if (transport != null) {
      transport.close();
    }
  }
}
