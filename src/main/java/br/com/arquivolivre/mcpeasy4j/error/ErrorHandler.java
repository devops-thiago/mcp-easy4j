package br.com.arquivolivre.mcpeasy4j.error;

import br.com.arquivolivre.jsonrpc.JsonRpcError;
import br.com.arquivolivre.jsonrpc.JsonRpcResponse;
import br.com.arquivolivre.jsonrpc.StandardErrors;

/**
 * Handles error scenarios and creates appropriate JSON-RPC error responses. This class provides
 * methods for each error category defined in the MCP framework.
 */
public class ErrorHandler {

  /**
   * Handles validation errors (missing or invalid parameters). Uses JSON-RPC error code -32602
   * (Invalid params).
   *
   * @param id the request ID for response correlation
   * @param message the error message describing the validation failure
   * @return a JSON-RPC error response with invalid params error
   */
  public JsonRpcResponse<?> handleValidationError(Object id, String message) {
    JsonRpcError<String> error = StandardErrors.invalidParams(message);
    return JsonRpcResponse.error(id, error);
  }

  /**
   * Handles invocation errors (exceptions thrown by user methods). Uses JSON-RPC error code -32603
   * (Internal error).
   *
   * @param id the request ID for response correlation
   * @param e the exception that was thrown during method invocation
   * @return a JSON-RPC error response with internal error
   */
  public JsonRpcResponse<?> handleInvocationError(Object id, Exception e) {
    JsonRpcError<String> error = StandardErrors.internalError(e.getMessage());
    return JsonRpcResponse.error(id, error);
  }

  /**
   * Handles protocol errors (invalid JSON-RPC messages). Uses JSON-RPC error code -32600 (Invalid
   * request).
   *
   * @param id the request ID for response correlation (may be null for malformed requests)
   * @param message the error message describing the protocol violation
   * @return a JSON-RPC error response with invalid request error
   */
  public JsonRpcResponse<?> handleProtocolError(Object id, String message) {
    JsonRpcError<String> error = StandardErrors.invalidRequest(message);
    return JsonRpcResponse.error(id, error);
  }

  /**
   * Handles method not found errors (unknown tool/resource/prompt). Uses JSON-RPC error code -32601
   * (Method not found).
   *
   * @param id the request ID for response correlation
   * @param methodName the name of the method that was not found
   * @return a JSON-RPC error response with method not found error
   */
  public JsonRpcResponse<?> handleMethodNotFound(Object id, String methodName) {
    String message = "Method not found: " + methodName;
    JsonRpcError<String> error = StandardErrors.methodNotFound(message);
    return JsonRpcResponse.error(id, error);
  }
}
