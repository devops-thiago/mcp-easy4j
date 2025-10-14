package br.com.arquivolivre.mcpeasy4j.registry;

import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/**
 * Registry for MCP tools that maintains insertion order. Stores tool definitions and provides
 * lookup capabilities.
 */
public class ToolRegistry {
  private final SequencedMap<String, ToolDefinition> tools = new LinkedHashMap<>();

  /**
   * Registers a tool definition.
   *
   * @param definition the tool definition to register
   */
  public void register(ToolDefinition definition) {
    tools.put(definition.name(), definition);
  }

  /**
   * Retrieves a tool by name.
   *
   * @param name the tool name
   * @return the tool definition, or null if not found
   */
  public ToolDefinition getTool(String name) {
    return tools.get(name);
  }

  /**
   * Returns all registered tools in insertion order.
   *
   * @return list of all tool definitions
   */
  public List<ToolDefinition> listTools() {
    return List.copyOf(tools.values());
  }
}
