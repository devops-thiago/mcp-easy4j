package br.com.arquivolivre.mcpeasy4j.scanner;

import br.com.arquivolivre.mcpeasy4j.annotation.Prompt;
import br.com.arquivolivre.mcpeasy4j.annotation.PromptArgument;
import br.com.arquivolivre.mcpeasy4j.annotation.Resource;
import br.com.arquivolivre.mcpeasy4j.annotation.Tool;
import br.com.arquivolivre.mcpeasy4j.model.PromptDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ResourceDefinition;
import br.com.arquivolivre.mcpeasy4j.model.ToolDefinition;
import br.com.arquivolivre.mcpeasy4j.schema.SchemaGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans classes for MCP annotations and creates definition objects. Discovers @Tool, @Resource,
 * and @Prompt annotated methods and generates the corresponding definition records for
 * registration.
 */
public class AnnotationScanner {

  private final SchemaGenerator schemaGenerator;

  public AnnotationScanner() {
    this.schemaGenerator = new SchemaGenerator();
  }

  /**
   * Scans an instance for @Tool annotated methods. Extracts tool metadata and generates input
   * schemas.
   *
   * @param instance the object instance to scan
   * @return list of ToolDefinition records
   */
  public List<ToolDefinition> scanTools(Object instance) {
    var tools = new ArrayList<ToolDefinition>();
    var clazz = instance.getClass();

    for (var method : clazz.getDeclaredMethods()) {
      var toolAnnotation = method.getAnnotation(Tool.class);

      if (toolAnnotation != null) {
        // Extract tool name from annotation or use method name
        var toolName = toolAnnotation.name().isEmpty() ? method.getName() : toolAnnotation.name();

        // Extract description
        var description = toolAnnotation.description();

        // Generate input schema using SchemaGenerator
        var inputSchema = schemaGenerator.generateSchema(method);

        // Create ToolDefinition
        var toolDef = new ToolDefinition(toolName, description, inputSchema, method, instance);

        tools.add(toolDef);
      }
    }

    return tools;
  }

  /**
   * Scans an instance for @Resource annotated methods. Extracts resource metadata from annotations.
   *
   * @param instance the object instance to scan
   * @return list of ResourceDefinition records
   */
  public List<ResourceDefinition> scanResources(Object instance) {
    var resources = new ArrayList<ResourceDefinition>();
    var clazz = instance.getClass();

    for (var method : clazz.getDeclaredMethods()) {
      var resourceAnnotation = method.getAnnotation(Resource.class);

      if (resourceAnnotation != null) {
        // Extract resource metadata from annotation
        var uri = resourceAnnotation.uri();
        var title = resourceAnnotation.title();
        var description = resourceAnnotation.description();
        var mimeType = resourceAnnotation.mimeType();

        // Create ResourceDefinition
        var resourceDef =
            new ResourceDefinition(uri, title, description, mimeType, method, instance);

        resources.add(resourceDef);
      }
    }

    return resources;
  }

  /**
   * Scans an instance for @Prompt annotated methods. Extracts prompt metadata and argument
   * definitions.
   *
   * @param instance the object instance to scan
   * @return list of PromptDefinition records
   */
  public List<PromptDefinition> scanPrompts(Object instance) {
    var prompts = new ArrayList<PromptDefinition>();
    var clazz = instance.getClass();

    for (var method : clazz.getDeclaredMethods()) {
      var promptAnnotation = method.getAnnotation(Prompt.class);

      if (promptAnnotation != null) {
        // Extract prompt metadata from annotation or use method name
        var name = promptAnnotation.name().isEmpty() ? method.getName() : promptAnnotation.name();
        var title = promptAnnotation.title();
        var description = promptAnnotation.description();

        // Extract @PromptArgument annotations from parameters
        var arguments = new ArrayList<br.com.arquivolivre.mcpeasy4j.model.PromptArgument>();
        var parameters = method.getParameters();

        for (var parameter : parameters) {
          var argAnnotation = parameter.getAnnotation(PromptArgument.class);

          if (argAnnotation != null) {
            // Extract argument name from annotation or parameter name
            var argName =
                argAnnotation.name().isEmpty() ? parameter.getName() : argAnnotation.name();

            var argDescription = argAnnotation.description();
            var required = argAnnotation.required();

            // Create PromptArgument record
            var arg =
                new br.com.arquivolivre.mcpeasy4j.model.PromptArgument(
                    argName, argDescription, required);

            arguments.add(arg);
          }
        }

        // Create PromptDefinition
        var promptDef = new PromptDefinition(name, title, description, arguments, method, instance);

        prompts.add(promptDef);
      }
    }

    return prompts;
  }
}
