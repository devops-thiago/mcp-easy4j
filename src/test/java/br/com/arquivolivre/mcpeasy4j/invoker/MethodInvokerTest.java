package br.com.arquivolivre.mcpeasy4j.invoker;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.mcpeasy4j.annotation.PromptArgument;
import br.com.arquivolivre.mcpeasy4j.annotation.Property;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for MethodInvoker. */
class MethodInvokerTest {

  private MethodInvoker invoker;
  private TestClass testInstance;

  @BeforeEach
  void setUp() {
    invoker = new MethodInvoker(new ObjectMapper());
    testInstance = new TestClass();
  }

  @Test
  void testInvokeWithStringParameter() throws Exception {
    var method = TestClass.class.getMethod("echo", String.class);
    Map<String, Object> params = Map.of("message", "Hello");

    var result = invoker.invoke(method, testInstance, params);

    assertEquals("Echo: Hello", result);
  }

  @Test
  void testInvokeWithMultipleParameters() throws Exception {
    var method = TestClass.class.getMethod("add", double.class, double.class);
    Map<String, Object> params = Map.of("a", 5.0, "b", 3.0);

    var result = invoker.invoke(method, testInstance, params);

    assertEquals(8.0, result);
  }

  @Test
  void testInvokeWithNullParameter() throws Exception {
    Method method = TestClass.class.getMethod("echo", String.class);
    Map<String, Object> params = Map.of();

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals("Echo: null", result);
  }

  @Test
  void testInvokeThrowsException() throws Exception {
    Method method = TestClass.class.getMethod("throwError");
    Map<String, Object> params = Map.of();

    assertThrows(
        MethodInvoker.InvocationException.class,
        () -> invoker.invoke(method, testInstance, params));
  }

  @Test
  void testInvokeWithBooleanParameter() throws Exception {
    Method method = TestClass.class.getMethod("negate", boolean.class);
    Map<String, Object> params = Map.of("value", true);

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals(false, result);
  }

  @Test
  void testInvokeReturnsNull() throws Exception {
    Method method = TestClass.class.getMethod("returnNull");
    Map<String, Object> params = Map.of();

    Object result = invoker.invoke(method, testInstance, params);

    assertNull(result);
  }

  @Test
  void testInvokeWithIntegerReturnType() throws Exception {
    Method method = TestClass.class.getMethod("getInteger");
    Map<String, Object> params = Map.of();

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals(42, result);
  }

  @Test
  void testInvokeWithLongReturnType() throws Exception {
    Method method = TestClass.class.getMethod("getLong");
    Map<String, Object> params = Map.of();

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals(123456789L, result);
  }

  @Test
  void testInvokeWithFloatReturnType() throws Exception {
    Method method = TestClass.class.getMethod("getFloat");
    Map<String, Object> params = Map.of();

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals(3.14f, result);
  }

  @Test
  void testInvokeWithComplexObjectParameter() throws Exception {
    Method method = TestClass.class.getMethod("processData", DataObject.class);
    Map<String, Object> params = Map.of("data", Map.of("name", "test", "value", 100));

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals("test:100", result);
  }

  @Test
  void testInvokeWithComplexObjectReturnValue() throws Exception {
    Method method = TestClass.class.getMethod("createData", String.class, int.class);
    Map<String, Object> params = Map.of("name", "example", "value", 50);

    Object result = invoker.invoke(method, testInstance, params);

    assertNotNull(result);
  }

  @Test
  void testInvokeWithPromptArgumentAnnotation() throws Exception {
    Method method = TestClass.class.getMethod("promptMethod", String.class);
    Map<String, Object> params = Map.of("promptArg", "test prompt");

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals("Prompt: test prompt", result);
  }

  @Test
  void testInvokeWithParameterNameFallback() throws Exception {
    Method method = TestClass.class.getMethod("noAnnotation", String.class);
    Map<String, Object> params = Map.of("arg0", "fallback test");

    Object result = invoker.invoke(method, testInstance, params);

    assertEquals("No annotation: fallback test", result);
  }

  @Test
  void testInvokeWithListReturnType() throws Exception {
    Method method = TestClass.class.getMethod("getList");
    Map<String, Object> params = Map.of();

    Object result = invoker.invoke(method, testInstance, params);

    assertNotNull(result);
  }

  static class DataObject {
    private String name;
    private int value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getValue() {
      return value;
    }

    public void setValue(int value) {
      this.value = value;
    }
  }

  static class TestClass {
    public String echo(@Property(name = "message") String message) {
      return "Echo: " + message;
    }

    public double add(@Property(name = "a") double a, @Property(name = "b") double b) {
      return a + b;
    }

    public boolean negate(@Property(name = "value") boolean value) {
      return !value;
    }

    public void throwError() {
      throw new RuntimeException("Test error");
    }

    public String returnNull() {
      return null;
    }

    public Integer getInteger() {
      return 42;
    }

    public Long getLong() {
      return 123456789L;
    }

    public Float getFloat() {
      return 3.14f;
    }

    public String processData(@Property(name = "data") DataObject data) {
      return data.getName() + ":" + data.getValue();
    }

    public DataObject createData(
        @Property(name = "name") String name, @Property(name = "value") int value) {
      DataObject obj = new DataObject();
      obj.setName(name);
      obj.setValue(value);
      return obj;
    }

    public String promptMethod(@PromptArgument(name = "promptArg") String arg) {
      return "Prompt: " + arg;
    }

    public String noAnnotation(String arg0) {
      return "No annotation: " + arg0;
    }

    public List<String> getList() {
      return List.of("item1", "item2", "item3");
    }
  }
}
