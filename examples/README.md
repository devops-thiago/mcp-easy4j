# MCP Easy4J Examples

This directory contains example MCP servers demonstrating how to use the MCP Easy4J framework.

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

## Building the Examples

From the `examples` directory, run:

```bash
mvn clean install
```

## Running the Example Server

### Using Maven

```bash
mvn exec:java
```

### Using Java directly

```bash
mvn package
java -jar target/mcp-easy4j-examples-1.0.0-SNAPSHOT.jar
```

## Example Server Features

The `ExampleMcpServer` demonstrates the following MCP Easy4J features:

### Tools

1. **echo** - Echoes back a message
   - Demonstrates basic `@Tool` and `@Property` usage
   - Parameters: `message` (required)

2. **add** - Adds two numbers together
   - Demonstrates numeric parameter handling
   - Parameters: `a` (required), `b` (required)

3. **greet** - Generates a personalized greeting
   - Demonstrates optional parameters
   - Parameters: `name` (required), `title` (optional)

4. **validate_email** - Validates email address format
   - Demonstrates format validation and structured responses
   - Parameters: `email` (required, format: email)

### Resources

1. **status://server** - Server status information
   - Returns current server status, timestamp, version, and uptime
   - MIME type: `application/json`

2. **system://info** - System information
   - Returns Java runtime and OS information
   - MIME type: `application/json`

### Prompts

1. **code_review** - Code review prompt generator
   - Generates prompts for code review with specific focus areas
   - Arguments: `language` (required), `focusArea` (optional)

2. **generate_docs** - Documentation generator prompt
   - Generates prompts for creating documentation
   - Arguments: `type` (required), `audience` (required), `format` (optional)

## Testing with MCP Inspector

You can test the example server using the [MCP Inspector](https://github.com/modelcontextprotocol/inspector):

1. Start the example server:
   ```bash
   mvn exec:java
   ```

2. In another terminal, use the MCP Inspector to connect to the server via stdio

3. Try calling the tools, accessing resources, and using prompts

## Creating Your Own MCP Server

To create your own MCP server:

1. Add the MCP Easy4J dependency to your `pom.xml`:
   ```xml
   <dependency>
       <groupId>io.github.mcpeasy4j</groupId>
       <artifactId>mcp-easy4j</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ```

2. Create a class annotated with `@McpServer`:
   ```java
   @McpServer(name = "my-server", version = "1.0.0")
   public class MyMcpServer {
       // Your tools, resources, and prompts here
   }
   ```

3. Add methods annotated with `@Tool`, `@Resource`, or `@Prompt`

4. Start the server:
   ```java
   public static void main(String[] args) {
       McpServerBootstrap.start(MyMcpServer.class);
   }
   ```

## Learn More

- [MCP Easy4J Documentation](../README.md)
- [Model Context Protocol Specification](https://modelcontextprotocol.io)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
