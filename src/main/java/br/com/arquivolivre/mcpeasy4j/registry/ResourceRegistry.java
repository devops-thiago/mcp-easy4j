package br.com.arquivolivre.mcpeasy4j.registry;

import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/**
 * Registry for MCP resources that maintains insertion order. Stores resource definitions and
 * provides lookup capabilities.
 */
public class ResourceRegistry {
  private final SequencedMap<String, ResourceDefinition> resources = new LinkedHashMap<>();

  /**
   * Registers a resource definition.
   *
   * @param definition the resource definition to register
   */
  public void register(ResourceDefinition definition) {
    resources.put(definition.uri(), definition);
  }

  /**
   * Retrieves a resource by URI.
   *
   * @param uri the resource URI
   * @return the resource definition, or null if not found
   */
  public ResourceDefinition getResource(String uri) {
    return resources.get(uri);
  }

  /**
   * Returns all registered resources in insertion order.
   *
   * @return list of all resource definitions
   */
  public List<ResourceDefinition> listResources() {
    return List.copyOf(resources.values());
  }

  /**
   * Returns resource templates for parameterized resources. Currently returns all resources; future
   * implementation may filter for resources with URI templates.
   *
   * @return list of resource templates
   */
  public List<ResourceDefinition> listTemplates() {
    return List.copyOf(resources.values());
  }
}
