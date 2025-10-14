package br.com.arquivolivre.mcpeasy4j.invoker;

import br.com.arquivolivre.mcpeasy4j.annotation.PromptArgument;
import br.com.arquivolivre.mcpeasy4j.annotation.Property;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Handles method invocation with parameter conversion and result serialization. Uses Jackson
 * ObjectMapper for JSON operations, consistent with the MCP SDK.
 */
public class MethodInvoker {
  private final ObjectMapper objectMapper;

  public MethodInvoker(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Invokes a method with the given parameters.
   *
   * @param method The method to invoke
   * @param instance The instance to invoke the method on
   * @param params The parameters as a map of parameter names to values
   * @return The serialized result as a JSON string
   * @throws InvocationException if invocation fails
   */
  public Object invoke(Method method, Object instance, Map<String, Object> params)
      throws InvocationException {
    try {
      // Convert parameters from Map to Object array
      Object[] args = convertParameters(method, params);

      // Invoke the method
      Object result = method.invoke(instance, args);

      // Serialize the result to JSON
      return convertToJson(result);
    } catch (IllegalAccessException e) {
      throw new InvocationException("Method is not accessible: " + method.getName(), e);
    } catch (InvocationTargetException e) {
      // Unwrap the actual exception thrown by the method
      Throwable cause = e.getCause();
      throw new InvocationException("Method invocation failed: " + cause.getMessage(), cause);
    } catch (Exception e) {
      throw new InvocationException("Failed to invoke method: " + e.getMessage(), e);
    }
  }

  /**
   * Converts a map of parameters to an array of objects matching method parameter types. Uses
   * parameter names to match map keys to method parameters. Tries @Property or @PromptArgument
   * annotation name first, then falls back to parameter name.
   *
   * @param method The method whose parameters to convert
   * @param params The parameter map
   * @return Array of converted parameter values
   */
  private Object[] convertParameters(Method method, Map<String, Object> params) {
    var parameters = method.getParameters();
    var args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      var param = parameters[i];

      // Try to get parameter name from annotation first
      var paramName = getParameterName(param);
      var value = params.get(paramName);

      if (value == null) {
        args[i] = null;
      } else {
        args[i] = convertParameter(value, param.getType());
      }
    }

    return args;
  }

  /**
   * Gets the parameter name from @Property or @PromptArgument annotation, or falls back to the
   * parameter's actual name.
   *
   * @param param The parameter
   * @return The parameter name to use for lookup
   */
  private String getParameterName(Parameter param) {
    // Check for @Property annotation
    var property = param.getAnnotation(Property.class);
    if (property != null && !property.name().isEmpty()) {
      return property.name();
    }

    // Check for @PromptArgument annotation
    var promptArg = param.getAnnotation(PromptArgument.class);
    if (promptArg != null && !promptArg.name().isEmpty()) {
      return promptArg.name();
    }

    // Fall back to parameter name from reflection
    return param.getName();
  }

  /**
   * Converts a single parameter value to the target type. Uses pattern matching for switch (Java
   * 21) to handle different type conversions.
   *
   * @param value The value to convert
   * @param targetType The target type
   * @return The converted value
   */
  private Object convertParameter(Object value, Class<?> targetType) {
    // If value is already the correct type, return it
    if (targetType.isInstance(value)) {
      return value;
    }

    // Convert using Jackson for complex objects
    return objectMapper.convertValue(value, targetType);
  }

  /**
   * Serializes a return value to JSON.
   *
   * @param result The result to serialize
   * @return The serialized result
   */
  private Object convertToJson(Object result) {
    if (result == null) {
      return null;
    }

    // For primitive types and strings, return as-is
    return switch (result) {
      case String s -> s;
      case Integer i -> i;
      case Long l -> l;
      case Double d -> d;
      case Float f -> f;
      case Boolean b -> b;
      default -> objectMapper.valueToTree(result);
    };
  }

  /** Exception thrown when method invocation fails. */
  public static class InvocationException extends Exception {
    public InvocationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
