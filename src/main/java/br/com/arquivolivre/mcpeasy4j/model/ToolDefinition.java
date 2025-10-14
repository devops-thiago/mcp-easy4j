package br.com.arquivolivre.mcpeasy4j.model;

import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Method;

/**
 * Immutable record representing a registered MCP tool. Contains tool metadata, input schema, and
 * the method to invoke. Uses MCP SDK's JsonSchema for protocol compliance.
 */
public record ToolDefinition(
    String name,
    String description,
    McpSchema.JsonSchema inputSchema,
    Method method,
    Object instance) {}
