package ch.unibas.dmi.dbis.cs108.phantomhunt.server;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServerAppTest {

  // helping method to test private method parsePortOrDefault
  private int invokeParsePort(String[] args, int defaultPort) throws Exception {
    Method method =
        ServerApp.class.getDeclaredMethod("parsePortOrDefault", String[].class, int.class);
    method.setAccessible(true);
    // since static method, object = null
    return (int) method.invoke(null, args, defaultPort);
  }

  @Test
  void privateConstructor_canBeCalledForCoverage() throws Exception {
    Constructor<ServerApp> constructor = ServerApp.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    ServerApp instance = constructor.newInstance();
    assertNotNull(instance);
  }

  @Test
  void parsePortOrDefault_nullArgs_returnsDefault() throws Exception {
    int result = invokeParsePort(null, 2222);
    assertEquals(2222, result, "Should return Default-Port, if args is null");
  }

  @Test
  void parsePortOrDefault_emptyArgs_returnsDefault() throws Exception {
    int result = invokeParsePort(new String[0], 2222);
    assertEquals(2222, result, "Should return Default-Port, if args is empty");
  }

  @Test
  void parsePortOrDefault_validPort_returnsDefault() throws Exception {
    int result = invokeParsePort(new String[] {"8080"}, 2222);
    assertEquals(8080, result, "Should parse the given port");
  }

  @Test
  void parsePortOrDefault_invalidFormat_returnsDefault() throws Exception {
    int result = invokeParsePort(new String[] {"NoNumber"}, 2222);
    assertEquals(2222, result, "Should handle error and return default port");
  }

  @Test
  void parsePortOrDefault_outOfBoundsTooLow_returnsDefault() throws Exception {
    // Port 0 or negativ
    int result = invokeParsePort(new String[] {"0"}, 2222);
    assertEquals(2222, result, "Should reject ports < 1.");

    int negativeResult = invokeParsePort(new String[] {"-1"}, 2222);
    assertEquals(2222, negativeResult, "Should reject negative ports.");
  }

  @Test
  void parsePortOrDefault_outOfBoundsTooHigh_returnsDefault() throws Exception {
    // Port over 65535
    int result = invokeParsePort(new String[] {"70000"}, 2222);
    assertEquals(2222, result, "Should reject ports > 65535.");
  }
}
