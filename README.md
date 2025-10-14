# MCP Easy4J

[![CI](https://github.com/devops-thiago/mcp-easy4j/actions/workflows/ci.yaml/badge.svg)](https://github.com/devops-thiago/mcp-easy4j/actions/workflows/ci.yaml)
[![codecov](https://codecov.io/gh/devops-thiago/mcp-easy4j/branch/main/graph/badge.svg)](https://codecov.io/gh/devops-thiago/mcp-easy4j)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_mcp-easy4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=devops-thiago_mcp-easy4j)
[![Maven Central](https://img.shields.io/maven-central/v/br.com.arquivolivre/mcp-easy4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22br.com.arquivolivre%22%20AND%20a:%22mcp-easy4j%22)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Annotation-based framework for building [Model Context Protocol](https://modelcontextprotocol.io/) servers in Java 21+.

## Features

- 🎯 Annotation-based API - Define tools, resources, and prompts with `@Tool`, `@Resource`, `@Prompt`
- 🚀 Zero boilerplate - No manual registration or protocol handling
- 🔄 Automatic schema generation from method signatures
- 📦 Built on official MCP Java SDK
- ⚡ Java 21 with virtual threads

## Quick Start

**Maven:**

```xml
<dependency>
    <groupId>br.com.arquivolivre</groupId>
    <artifactId>mcp-easy4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

```gradle
dependencies {
    implementation 'br.com.arquivolivre:mcp-easy4j:1.0.0'
}
```

**Gradle (Kotlin DSL):**

```kotlin
dependencies {
    implementation("br.com.arquivolivre:mcp-easy4j:1.0.0")
}
```

**Create your server:**

```java
@McpServer(name = "my-server", version = "1.0.0")
public class MyServer {

    @Tool(description = "Echoes a message")
    public String echo(@Property(description = "Message to echo") String message) {
        return "Echo: " + message;
    }

    @Resource(uri = "status://server", description = "Server status")
    public Map<String, Object> getStatus() {
        return Map.of("status", "running", "timestamp", LocalDateTime.now());
    }

    @Prompt(name = "code_review", description = "Code review prompt")
    public String codeReview(@PromptArgument(description = "Language") String language) {
        return "Please review the following " + language + " code...";
    }

    public static void main(String[] args) {
        McpServerBootstrap.start(MyServer.class);
    }
}
```

**Run:**

```bash
mvn exec:java -Dexec.mainClass="com.example.MyServer"
```

## Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@McpServer` | Mark server class | `@McpServer(name = "my-server")` |
| `@Tool` | Define a tool | `@Tool(description = "Does something")` |
| `@Property` | Tool parameter | `@Property(description = "Input", required = true)` |
| `@Resource` | Define a resource | `@Resource(uri = "file://data", mimeType = "application/json")` |
| `@Prompt` | Define a prompt | `@Prompt(name = "my_prompt")` |
| `@PromptArgument` | Prompt parameter | `@PromptArgument(description = "Argument")` |

## Type Mapping

| Java Type | JSON Schema |
|-----------|-------------|
| `String` | `string` |
| `int`, `Integer`, `long`, `Long` | `integer` |
| `double`, `Double`, `float`, `Float` | `number` |
| `boolean`, `Boolean` | `boolean` |
| `Map`, `Object` | `object` |
| `List`, `Array` | `array` |

## Examples

See [examples/](examples/) directory for complete working examples including:
- Tools with various parameter types
- Resources with structured data
- Prompts with arguments

Run the example:

```bash
cd examples && mvn exec:java
```

## Architecture

MCP Easy4J is a thin annotation wrapper around the [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk):

```
Your @Tool/@Resource/@Prompt methods
           ↓
    MCP Easy4J (annotation scanning)
           ↓
    MCP Java SDK (protocol & transport)
           ↓
    stdin/stdout (JSON-RPC 2.0)
```

The SDK handles all protocol details - we just make it easier to use with annotations.

## Building

```bash
mvn clean install
mvn test
```

## License

MIT License - see [LICENSE](LICENSE) file.

## Contributing

Contributions welcome! Open an issue or submit a PR.

## Links

- [MCP Specification](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Examples](examples/)
