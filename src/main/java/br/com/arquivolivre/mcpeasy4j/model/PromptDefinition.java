package br.com.arquivolivre.mcpeasy4j.model;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Immutable record representing a registered MCP prompt. Contains prompt metadata, arguments, and
 * the method to invoke.
 */
public record PromptDefinition(
    String name,
    String title,
    String description,
    List<PromptArgument> arguments,
    Method method,
    Object instance) {}
