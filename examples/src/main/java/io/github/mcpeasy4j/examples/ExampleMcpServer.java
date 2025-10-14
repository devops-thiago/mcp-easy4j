package io.github.mcpeasy4j.examples;

import br.com.arquivolivre.mcpeasy4j.McpServerBootstrap;
import br.com.arquivolivre.mcpeasy4j.annotation.McpServer;
import br.com.arquivolivre.mcpeasy4j.annotation.Prompt;
import br.com.arquivolivre.mcpeasy4j.annotation.PromptArgument;
import br.com.arquivolivre.mcpeasy4j.annotation.Property;
import br.com.arquivolivre.mcpeasy4j.annotation.Resource;
import br.com.arquivolivre.mcpeasy4j.annotation.Tool;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Example MCP server demonstrating the annotation-based framework. This server provides tools,
 * resources, and prompts for demonstration purposes.
 */
@McpServer(name = "example-server", version = "1.0.0", enableResources = true, enablePrompts = true)
public class ExampleMcpServer {

  /**
   * Example tool that echoes a message back to the caller. Demonstrates basic @Tool and @Property
   * usage.
   */
  @Tool(description = "Echoes back the provided message")
  public String echo(
      @Property(description = "The message to echo back", required = true) String message) {
    return "Echo: " + message;
  }

  /** Example tool that adds two numbers. Demonstrates numeric parameter handling. */
  @Tool(description = "Adds two numbers together")
  public double add(
      @Property(description = "First number", required = true) double a,
      @Property(description = "Second number", required = true) double b) {
    return a + b;
  }

  /** Example tool that formats a greeting message. Demonstrates optional parameters. */
  @Tool(description = "Generates a personalized greeting message")
  public String greet(
      @Property(description = "Name of the person to greet", required = true) String name,
      @Property(description = "Optional title (e.g., Mr., Dr.)", required = false) String title) {
    if (title != null && !title.isEmpty()) {
      return "Hello, " + title + " " + name + "!";
    }
    return "Hello, " + name + "!";
  }

  /**
   * Example tool that validates an email address format. Demonstrates format validation in
   * parameters.
   */
  @Tool(
      name = "validate_email",
      description = "Validates if a string is a properly formatted email address")
  public Map<String, Object> validateEmail(
      @Property(description = "Email address to validate", required = true, format = "email")
          String email) {
    boolean isValid =
        email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    return Map.of(
        "email", email,
        "isValid", isValid,
        "message", isValid ? "Valid email format" : "Invalid email format");
  }

  /**
   * Example resource that provides server status information. Demonstrates basic @Resource usage.
   */
  @Resource(
      uri = "status://server",
      title = "Server Status",
      description = "Current status and information about the MCP server",
      mimeType = "application/json")
  public Map<String, Object> getServerStatus() {
    return Map.of(
        "status", "running",
        "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        "version", "1.0.0",
        "uptime", "N/A");
  }

  /**
   * Example resource that provides system information. Demonstrates resource with structured data.
   */
  @Resource(
      uri = "system://info",
      title = "System Information",
      description = "Information about the Java runtime environment",
      mimeType = "application/json")
  public Map<String, Object> getSystemInfo() {
    return Map.of(
        "javaVersion", System.getProperty("java.version"),
        "javaVendor", System.getProperty("java.vendor"),
        "osName", System.getProperty("os.name"),
        "osVersion", System.getProperty("os.version"),
        "osArch", System.getProperty("os.arch"));
  }

  /**
   * Example prompt for generating code review comments. Demonstrates @Prompt and @PromptArgument
   * usage.
   */
  @Prompt(
      name = "code_review",
      title = "Code Review Prompt",
      description = "Generates a prompt for reviewing code with specific focus areas")
  public String generateCodeReviewPrompt(
      @PromptArgument(description = "Programming language of the code", required = true)
          String language,
      @PromptArgument(
              description = "Specific area to focus on (e.g., security, performance)",
              required = false)
          String focusArea) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Please review the following ").append(language).append(" code");

    if (focusArea != null && !focusArea.isEmpty()) {
      prompt.append(" with a focus on ").append(focusArea);
    }

    prompt.append(".\n\nProvide feedback on:\n");
    prompt.append("- Code quality and readability\n");
    prompt.append("- Best practices and conventions\n");
    prompt.append("- Potential bugs or issues\n");

    if (focusArea != null && !focusArea.isEmpty()) {
      prompt
          .append("- ")
          .append(focusArea.substring(0, 1).toUpperCase(java.util.Locale.ROOT))
          .append(focusArea.substring(1))
          .append(" considerations\n");
    }

    return prompt.toString();
  }

  /** Example prompt for generating documentation. Demonstrates prompt with multiple arguments. */
  @Prompt(
      name = "generate_docs",
      title = "Documentation Generator",
      description = "Generates a prompt for creating documentation")
  public String generateDocsPrompt(
      @PromptArgument(
              description = "Type of documentation (API, user guide, etc.)",
              required = true)
          String type,
      @PromptArgument(
              description = "Target audience (developers, end-users, etc.)",
              required = true)
          String audience,
      @PromptArgument(description = "Output format (markdown, HTML, etc.)", required = false)
          String format) {
    String outputFormat = (format != null && !format.isEmpty()) ? format : "markdown";

    return String.format(
        "Generate %s documentation for %s.%n%n"
            + "Requirements:%n"
            + "- Clear and concise language%n"
            + "- Include examples where appropriate%n"
            + "- Format: %s%n"
            + "- Target audience: %s%n%n"
            + "Please ensure the documentation is comprehensive and easy to understand.",
        type, audience, outputFormat, audience);
  }

  /**
   * Main method to start the MCP server. This demonstrates how to bootstrap the server using the
   * framework.
   */
  public static void main(String[] args) {
    System.err.println("Starting Example MCP Server...");
    McpServerBootstrap.start(ExampleMcpServer.class);
  }
}
