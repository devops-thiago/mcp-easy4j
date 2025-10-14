# MCP Easy4J

[![CI](https://github.com/devops-thiago/mcp-easy4j/actions/workflows/ci.yaml/badge.svg)](https://github.com/devops-thiago/mcp-easy4j/actions/workflows/ci.yaml)
[![codecov](https://codecov.io/gh/devops-thiago/mcp-easy4j/branch/main/graph/badge.svg)](https://codecov.io/gh/devops-thiago/mcp-easy4j)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_mcp-easy4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=devops-thiago_mcp-easy4j)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.mcpeasy4j/mcp-easy4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.mcpeasy4j%22%20AND%20a:%22mcp-easy4j%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An annotation-based framework for building Model Context Protocol (MCP) servers in Java.

## Overview

MCP Easy4J simplifies MCP server development by allowing developers to define tools, resources, and prompts using Java annotations. This eliminates boilerplate code and provides a declarative approach to server configuration.

## Features

- 🎯 **Annotation-Based**: Define tools, resources, and prompts with simple annotations
- 🚀 **Zero Boilerplate**: No manual registration or protocol handling code needed
- 🔄 **Automatic Schema Generation**: JSON schemas generated from method signatures
- 🎨 **Type Safe**: Leverage Java's type system for compile-time safety
- ⚡ **Virtual Threads**: Uses Java 21 virtual threads for efficient I/O
- 📦 **MCP Compliant**: Built on the official MCP Java SDK

## Requirements

- Java 21 or higher
- Maven 3.6+

## Quick Start

### 1. Add Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.mcpeasy4j</groupId>
    <artifactId>mcp-easy4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Create Your MCP Server

```java
import br.com.arquivolivre.mcpeasy4j.MCPServerBootstrap;
import br.com.arquivolivre.mcpeasy4j.annotation.*;

@MCPServer(
    name = "my-server",
    version = "1.0.0"
)
public class MyMCPServer {

    @Tool(description = "Echoes back the provided message")
    public String echo(
        @Property(name = "message", description = "The message to echo")
        String message
    ) {
        return "Echo: " + message;
    }

    @Resource(
        uri = "status://server",
        title = "Server Status",
        description = "Current server status"
    )
    public Map<String, Object> getStatus() {
        return Map.of(
            "status", "running",
            "timestamp", LocalDateTime.now().toString()
        );
    }

    @Prompt(
        name = "code_review",
        description = "Generates a code review prompt"
    )
    public String codeReview(
        @PromptArgument(name = "language", description = "Programming language")
        String language
    ) {
        return "Please review the following " + language + " code...";
    }

    public static void main(String[] args) {
        MCPServerBootstrap.start(MyMCPServer.class);
    }
}
```

### 3. Run Your Server

```bash
mvn compile exec:java -Dexec.mainClass="com.example.MyMCPServer"
```

## Annotations

### @MCPServer

Marks a class as an MCP server entry point.

```java
@MCPServer(
    name = "my-server",           // Server name (optional)
    version = "1.0.0",            // Server version (default: "1.0.0")
    enableResources = true,       // Enable resources (default: true)
    enablePrompts = true          // Enable prompts (default: true)
)
```

### @Tool

Defines an MCP tool from a method.

```java
@Tool(
    name = "my_tool",             // Tool name (defaults to method name)
    description = "Tool description"
)
public String myTool(
    @Property(name = "param", description = "Parameter description")
    String param
) {
    return "result";
}
```

### @Property

Defines a tool parameter with schema information.

```java
@Property(
    name = "email",               // Parameter name (defaults to param name)
    description = "Email address",
    required = true,              // Is required (default: true)
    format = "email"              // Format constraint (optional)
)
String email
```

### @Resource

Defines an MCP resource from a method.

```java
@Resource(
    uri = "file://data.json",     // Resource URI
    title = "Data File",          // Resource title
    description = "Description",  // Resource description
    mimeType = "application/json" // MIME type (default: "text/plain")
)
public String getData() {
    return "{ \"data\": \"value\" }";
}
```

### @Prompt

Defines an MCP prompt from a method.

```java
@Prompt(
    name = "my_prompt",           // Prompt name
    title = "My Prompt",          // Prompt title
    description = "Description"   // Prompt description
)
public String myPrompt(
    @PromptArgument(name = "arg", description = "Argument description")
    String arg
) {
    return "Generated prompt text...";
}
```

### @PromptArgument

Defines a prompt argument.

```java
@PromptArgument(
    name = "language",            // Argument name (defaults to param name)
    description = "Programming language",
    required = true               // Is required (default: true)
)
String language
```

## Type Mapping

Java types are automatically mapped to JSON schema types:

| Java Type | JSON Schema Type |
|-----------|------------------|
| `String` | `"string"` |
| `int`, `Integer`, `long`, `Long` | `"integer"` |
| `double`, `Double`, `float`, `Float` | `"number"` |
| `boolean`, `Boolean` | `"boolean"` |
| `Object`, `Map` | `"object"` |
| `List`, `Array` | `"array"` |

## Communication Protocol

MCP Easy4J uses the MCP Java SDK's `StdioServerTransportProvider` for stdin/stdout communication following the JSON-RPC 2.0 protocol. The SDK handles all protocol-level details including:

- JSON-RPC 2.0 request/response serialization
- Error handling and error response formatting
- Protocol compliance with MCP specification
- Transport lifecycle management

**Request (stdin):**
```json
{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"echo","arguments":{"message":"Hello"}}}
```

**Response (stdout):**
```json
{"jsonrpc":"2.0","id":1,"result":{"content":[{"type":"text","text":"Echo: Hello"}]}}
```

The framework automatically registers your annotated methods with the SDK server, which handles all request routing and response formatting.

## Supported MCP Methods

- `tools/list` - List all available tools
- `tools/call` - Invoke a specific tool
- `resources/list` - List all available resources
- `resources/read` - Read a specific resource
- `resources/templates/list` - List resource templates
- `prompts/list` - List all available prompts
- `prompts/get` - Get a specific prompt

## Example Server

See the complete example in the `examples/` directory. The example demonstrates:
- Tools with various parameter types
- Resources with structured data
- Prompts with arguments
- Error handling and validation

To run the example:

```bash
cd examples
mvn clean compile exec:java
```

For more details, see [examples/README.md](examples/README.md).

## Error Handling

The MCP Java SDK automatically handles errors and returns appropriate JSON-RPC error responses:

- `-32700`: Parse error (invalid JSON)
- `-32600`: Invalid request
- `-32601`: Method not found
- `-32602`: Invalid params
- `-32603`: Internal error

The framework catches exceptions from your annotated methods and converts them to proper error responses via the SDK.

## Migration Notes

### Version 2.0+ (SDK Transport Integration)

Starting with version 2.0, MCP Easy4J uses the official MCP Java SDK's transport layer instead of a custom implementation. This change provides:

- **Better Protocol Compliance**: SDK ensures full MCP specification compliance
- **Improved Reliability**: Leverages battle-tested SDK transport implementation
- **Future Features**: Automatic access to new SDK features and improvements
- **Reduced Maintenance**: Less custom code to maintain

**Breaking Changes:** None for end users. The annotation-based API remains unchanged. All existing code continues to work without modifications.

**Internal Changes:**
- Removed custom `StdioTransport` implementation
- Removed custom `McpRequestHandler`
- Added `SdkFeatureAdapter` to bridge annotations to SDK registration
- Updated `McpServerBootstrap` to use SDK's `StdioServerTransportProvider`

If you were extending internal framework classes (not recommended), you may need to update your code.

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/mcp-easy4j.git
cd mcp-easy4j

# Build the project
mvn clean install

# Run tests
mvn test

# Run the example server
cd examples
mvn exec:java
```

## Architecture

MCP Easy4J is a thin annotation-based wrapper around the official MCP Java SDK. The framework's sole purpose is to make it easier to create MCP servers using annotations instead of manually implementing the SDK's APIs. All protocol handling, transport, and server implementation is delegated to the SDK.

```
┌─────────────────────────────────────────────────┐
│              MCP Client                         │
└────────────────┬────────────────────────────────┘
                 │ stdin/stdout (JSON-RPC)
┌────────────────▼────────────────────────────────┐
│   MCP Java SDK Transport Layer                  │
│   (StdioServerTransportProvider)                │
│   - Handles JSON-RPC protocol                   │
│   - Manages stdin/stdout communication          │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│   MCP Java SDK Server                           │
│   (McpStatelessSyncServer)                      │
│   - Routes requests to registered features      │
│   - Handles protocol compliance                 │
└────┬───────────┬───────────┬────────────────────┘
     │           │           │
┌────▼────┐ ┌───▼─────┐ ┌──▼──────┐
│  Tool   │ │Resource │ │ Prompt  │
│Handlers │ │Handlers │ │Handlers │
└────┬────┘ └───┬─────┘ └──┬──────┘
     │          │           │
     └──────────┴───────────┘
                │
┌───────────────▼────────────────────────────────┐
│   MCP Easy4J Framework (Thin Layer)            │
│   - SdkFeatureAdapter (registers features)     │
│   - MethodInvoker (invokes your methods)       │
│   - AnnotationScanner (discovers annotations)  │
└────────────────┬───────────────────────────────┘
                 │
┌────────────────▼───────────────────────────────┐
│      Your Annotated Methods                    │
│  (@Tool, @Resource, @Prompt)                   │
└────────────────────────────────────────────────┘
```

**Key Benefits:**
- **SDK-Powered**: Leverages the official MCP Java SDK for protocol compliance and transport
- **Annotation-Based**: Simple declarative API using Java annotations
- **Minimal Overhead**: Thin wrapper that adds minimal abstraction
- **Future-Proof**: Automatically benefits from SDK improvements and updates

## Dependencies

MCP Easy4J is built on top of the official MCP Java SDK:

- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) - Official MCP protocol implementation
  - Provides `StdioServerTransportProvider` for stdin/stdout communication
  - Provides `McpStatelessSyncServer` for server implementation
  - Handles JSON-RPC 2.0 protocol compliance
  - Manages session lifecycle and error handling
- [Gson](https://github.com/google/gson) - JSON serialization for parameter mapping

## License

[Add your license here]

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and questions, please open an issue on GitHub.

## Acknowledgments

Built on top of the [Model Context Protocol](https://modelcontextprotocol.io/) specification.
