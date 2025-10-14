package br.com.arquivolivre.mcpeasy4j.model;

import java.lang.reflect.Method;

/**
 * Immutable record representing a registered MCP resource. Contains resource metadata and the
 * method to invoke for resource access.
 */
public record ResourceDefinition(
    String uri,
    String title,
    String description,
    String mimeType,
    Method method,
    Object instance) {}
