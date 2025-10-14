package br.com.arquivolivre.mcpeasy4j.error;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.jsonrpc.JsonRpcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for ErrorHandler. */
class ErrorHandlerTest {

  private ErrorHandler errorHandler;

  @BeforeEach
  void setUp() {
    errorHandler = new ErrorHandler();
  }

  @Test
  void testHandleValidationError() {
    JsonRpcResponse<?> response = errorHandler.handleValidationError(1, "Missing parameter");

    assertNotNull(response);
    assertTrue(response.getError().isPresent());
    assertEquals(-32602, response.getError().get().getCode());
    assertTrue(response.getError().get().getMessage().contains("Invalid params"));
  }

  @Test
  void testHandleInvocationError() {
    Exception exception = new RuntimeException("Test error");
    JsonRpcResponse<?> response = errorHandler.handleInvocationError(2, exception);

    assertNotNull(response);
    assertTrue(response.getError().isPresent());
    assertEquals(-32603, response.getError().get().getCode());
  }

  @Test
  void testHandleProtocolError() {
    JsonRpcResponse<?> response = errorHandler.handleProtocolError(3, "Invalid JSON");

    assertNotNull(response);
    assertTrue(response.getError().isPresent());
    assertEquals(-32600, response.getError().get().getCode());
  }

  @Test
  void testHandleMethodNotFound() {
    JsonRpcResponse<?> response = errorHandler.handleMethodNotFound(4, "unknownMethod");

    assertNotNull(response);
    assertTrue(response.getError().isPresent());
    assertEquals(-32601, response.getError().get().getCode());
    // The message contains "Method not found: unknownMethod"
    assertTrue(response.getError().get().getMessage().contains("Method not found"));
  }

  @Test
  void testHandleErrorWithNullId() {
    JsonRpcResponse<?> response = errorHandler.handleValidationError(null, "Error");

    assertNotNull(response);
    assertTrue(response.getError().isPresent());
  }
}
