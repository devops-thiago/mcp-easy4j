package br.com.arquivolivre.mcpeasy4j.registry;

import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/**
 * Registry for MCP prompts that maintains insertion order. Stores prompt definitions and provides
 * lookup capabilities.
 */
public class PromptRegistry {
  private final SequencedMap<String, PromptDefinition> prompts = new LinkedHashMap<>();

  /**
   * Registers a prompt definition.
   *
   * @param definition the prompt definition to register
   */
  public void register(PromptDefinition definition) {
    prompts.put(definition.name(), definition);
  }

  /**
   * Retrieves a prompt by name.
   *
   * @param name the prompt name
   * @return the prompt definition, or null if not found
   */
  public PromptDefinition getPrompt(String name) {
    return prompts.get(name);
  }

  /**
   * Returns all registered prompts in insertion order.
   *
   * @return list of all prompt definitions
   */
  public List<PromptDefinition> listPrompts() {
    return List.copyOf(prompts.values());
  }
}
