package br.com.arquivolivre.mcpeasy4j.adapter;

import br.com.arquivolivre.mcpeasy4j.invoker.MethodInvoker;
import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.List;
import java.util.Map;

/**
 * Adapter that bridges annotation-based definitions to SDK feature registration. Converts
 * ToolDefinition, ResourceDefinition, and PromptDefinition to SDK specifications and registers them
 * with the MCP server. Uses Jackson ObjectMapper for JSON operations, consistent with the MCP SDK.
 */
public class SdkFeatureAdapter {
  private final MethodInvoker methodInvoker;
  private final ObjectMapper objectMapper;

  public SdkFeatureAdapter(ObjectMapper objectMapper) {
    this.methodInvoker = new MethodInvoker(objectMapper);
    this.objectMapper = objectMapper;
  }

  /**
   * Registers tools with the SDK server.
   *
   * @param server The MCP server to register tools with
   * @param tools List of tool definitions to register
   */
  public void registerTools(McpSyncServer server, List<ToolDefinition> tools) {
    for (var toolDef : tools) {
      // Create SDK Tool specification
      var tool =
          new Tool(
              toolDef.name(),
              null, // title (optional)
              toolDef.description(),
              toolDef.inputSchema(),
              null, // outputSchema (optional)
              null, // annotations (optional)
              null // meta (optional)
              );

      // Create specification with handler
      var spec =
          new SyncToolSpecification(
              tool,
              (exchange, arguments) -> {
                try {
                  var result =
                      methodInvoker.invoke(toolDef.method(), toolDef.instance(), arguments);

                  // Convert result to CallToolResult
                  return createCallToolResult(result);
                } catch (MethodInvoker.InvocationException e) {
                  throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
                }
              });

      // Register with server
      server.addTool(spec);
    }
  }

  /**
   * Registers resources with the SDK server.
   *
   * @param server The MCP server to register resources with
   * @param resources List of resource definitions to register
   */
  public void registerResources(McpSyncServer server, List<ResourceDefinition> resources) {
    for (var resourceDef : resources) {
      // Create SDK Resource specification
      var resource =
          new Resource(
              resourceDef.uri(),
              resourceDef.title(),
              resourceDef.description(),
              resourceDef.mimeType(),
              null // annotations (optional)
              );

      // Create specification with handler
      var spec =
          new SyncResourceSpecification(
              resource,
              (exchange, request) -> {
                try {
                  // Resources don't take parameters, just invoke the method
                  var result =
                      methodInvoker.invoke(resourceDef.method(), resourceDef.instance(), Map.of());

                  // Convert result to ReadResourceResult
                  return createReadResourceResult(
                      result, resourceDef.uri(), resourceDef.mimeType());
                } catch (MethodInvoker.InvocationException e) {
                  throw new RuntimeException("Resource read failed: " + e.getMessage(), e);
                }
              });

      // Register with server
      server.addResource(spec);
    }
  }

  /**
   * Registers prompts with the SDK server.
   *
   * @param server The MCP server to register prompts with
   * @param prompts List of prompt definitions to register
   */
  public void registerPrompts(McpSyncServer server, List<PromptDefinition> prompts) {
    for (var promptDef : prompts) {
      // Convert our PromptArgument to SDK PromptArgument
      var sdkArguments =
          promptDef.arguments().stream()
              .map(
                  arg ->
                      new McpSchema.PromptArgument(arg.name(), arg.description(), arg.required()))
              .toList();

      // Create SDK Prompt specification
      var prompt =
          new McpSchema.Prompt(
              promptDef.name(),
              promptDef.title(),
              promptDef.description(),
              sdkArguments,
              null // meta (optional)
              );

      // Create specification with handler
      var spec =
          new SyncPromptSpecification(
              prompt,
              (exchange, request) -> {
                try {
                  var result =
                      methodInvoker.invoke(
                          promptDef.method(), promptDef.instance(), request.arguments());

                  // Convert result to GetPromptResult
                  return createGetPromptResult(result, promptDef.description());
                } catch (MethodInvoker.InvocationException e) {
                  throw new RuntimeException("Prompt execution failed: " + e.getMessage(), e);
                }
              });

      // Register with server
      server.addPrompt(spec);
    }
  }

  /**
   * Converts method result to CallToolResult. Handles different result types: - String: returned as
   * text content - JsonElement: converted to JSON string - Other objects: serialized to JSON string
   *
   * @param result The result from method invocation
   * @return CallToolResult with the result as text content
   */
  private CallToolResult createCallToolResult(Object result) {
    var resultText = convertResultToString(result);
    var content = new TextContent(resultText);
    return new CallToolResult(List.of(content), false);
  }

  /**
   * Converts method result to ReadResourceResult. Handles different result types similar to
   * CallToolResult.
   *
   * @param result The result from method invocation
   * @param uri The URI of the resource
   * @param mimeType The MIME type of the resource
   * @return ReadResourceResult with the result as text resource contents
   */
  private ReadResourceResult createReadResourceResult(Object result, String uri, String mimeType) {
    var resultText = convertResultToString(result);
    var contents = new TextResourceContents(uri, mimeType, resultText);
    return new ReadResourceResult(List.of(contents));
  }

  /**
   * Converts method result to GetPromptResult. The result is expected to be a string or will be
   * converted to one.
   *
   * @param result The result from method invocation
   * @param description The prompt description
   * @return GetPromptResult with the result as a user message
   */
  private GetPromptResult createGetPromptResult(Object result, String description) {
    var resultText = convertResultToString(result);
    var content = new TextContent(resultText);
    var message = new PromptMessage(Role.USER, content);
    return new GetPromptResult(description, List.of(message));
  }

  /**
   * Converts a result object to a string representation. Handles different types appropriately: -
   * null: empty string - String: returned as-is - JsonElement: converted to JSON string - Other:
   * serialized to JSON
   *
   * @param result The result to convert
   * @return String representation of the result
   */
  private String convertResultToString(Object result) {
    if (result == null) {
      return "";
    }
    if (result instanceof String str) {
      return str;
    }
    if (result instanceof JsonNode jsonNode) {
      return jsonNode.toString();
    }
    // For other objects, serialize to JSON
    try {
      return objectMapper.writeValueAsString(result);
    } catch (Exception e) {
      return result.toString();
    }
  }
}
