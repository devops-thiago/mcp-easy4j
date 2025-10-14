package br.com.arquivolivre.mcpeasy4j.model;

/**
 * Immutable record representing a prompt argument definition. Used to describe parameters that can
 * be passed to prompts.
 */
public record PromptArgument(String name, String description, boolean required) {}
